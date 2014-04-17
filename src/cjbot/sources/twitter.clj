(ns cjbot.sources.twitter
  (:use
   [cjbot.redis]
   [clojure.set]
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful]
   [twitter.api.search]
   [carica.core])
  (:require 
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)])
  )

(def source-name "twitter")

(def creds (make-oauth-creds (config :sources :twitter :creds :app-consumer-key)
                             (config :sources :twitter :creds :app-consumer-secret)
                             (config :sources :twitter :creds :user-access-token) 
                             (config :sources :twitter :creds :user-access-token-secret)))

;;
;;

(defn sleep [n]
  (warn "Sleeping for" n "milliseconds")
  (Thread/sleep n))

(defn get-hashtags-from-timeline [timeline]
  (distinct (map clojure.string/lower-case (map :text (flatten (map :hashtags (map :entities (timeline :body))))))))

(defn get-people-mentions-out-from-timeline [timeline]
  (distinct (map :screen_name (flatten (map :user_mentions (map :entities (timeline :body)))))))

(defn get-people-retweets-out-from-timeline [timeline]
  ;;(map :user (map :retweeted_status (filter #(not (nil? (% :retweeted_status))) (timeline :body)))))
  (keep identity (flatten (map :user (map :retweeted_status (timeline :body))))))

(defn get-urls-from-timeline [timeline]
  (let [url_objects_no_retweets (flatten (map :urls (map :entities (timeline :body))))
        url_objects_retweets (flatten (map :urls (map  :entities (map :retweeted_status (timeline :body)))))
        ]
    (distinct (map :expanded_url (concat url_objects_no_retweets url_objects_retweets)))))

;;
;;  get-user-followers
;;

(defn get-user-followers [ & {:keys [twitter-params crawler-params]}]
  (warn "Retreiving followers for user-id =" (twitter-params :user-id) "screen-name = " (twitter-params :screen-name))
  (def cursor (atom -1))
  (def users (atom ()))
  (while (not= @cursor 0)
    (do (warn @cursor)
        (def body ((followers-ids :oauth-creds creds 
                              :proxy (config :proxy)
                              :params twitter-params) :body))

        (reset! users (concat @users (body :ids)))
        (reset! cursor (body :next_cursor))

        (when (re-find #"stdout" (crawler-params :output))
           (doall (map #(println "follower" (twitter-params :user-id) %) (body :ids)  )))

        (when (re-find #"redis" (crawler-params :output))
           (save-key-value-hash source-name "user" (twitter-params :user-id) "followers" (body :ids) ))

        (sleep (crawler-params :sleep))))

  (warn "Found" (count @users) "followers for user-id ="  (twitter-params :user-id) "screen-name = " (twitter-params :screen-name))
  @users)

;;
;;  get-user-friends
;;

(defn get-user-friends [ & {:keys [twitter-params crawler-params]}]
  (warn "Retreiving friends for user-id =" (twitter-params :user-id) "screen-name = " (twitter-params :screen-name))
  (def cursor (atom -1))
  (def users (atom ()))
  (while (not= @cursor 0)
    (do (warn @cursor)
        (def body ((friends-ids :oauth-creds creds 
                              :proxy (config :proxy)
                              :params twitter-params) :body))

        (reset! users (concat @users (body :ids)))
        (reset! cursor (body :next_cursor))

        (when (re-find #"stdout" (crawler-params :output))
           (doall (map #(println "friends" (twitter-params :user-id) %) (body :ids)  )))

        (when (re-find #"redis" (crawler-params :output))
           (save-key-value-hash source-name "user" (twitter-params :user-id) "friends" (body :ids) ))

        (sleep (crawler-params :sleep))))

  (warn "Found" (count @users) "friend for user-id ="  (twitter-params :user-id) "screen-name = " (twitter-params :screen-name))
  @users)

(defn get-user-timeline [twitter-params]
  (warn "Retreiving timeline for user-id =" (twitter-params :user-id) "screen-name = " (twitter-params :screen-name))
  (def timeline ((statuses-user-timeline :oauth-creds creds 
                                         :proxy (config :proxy)
                                         :params (merge twitter-params 
                                                        {:count 5000 :include-rts true}))
    :body))
  (warn "Found" (count timeline) "tweets in timeline for user-id =" (twitter-params :user-id) "screen-name = " (twitter-params :screen-name))
  timeline)

(defn get-users-lookup [twitter-params]
  (warn "Retreiving timeline for user-id =" (twitter-params :user-id) "screen-name = " (twitter-params :screen-name))
  (def result ((users-lookup :oauth-creds creds 
                             :proxy (config :proxy)
                             :params twitter-params)
    :body))
  result)

(defn get-usersss-lookup [people]
  ;; split in list of 100 people
  ;; (users-lookup {user-id "1,1,1,1"})
  nil
  )

        ;;people-retwitting (get-retwitters-out-from-timeline timeline)
        ;;mentions-names-out (get-mentions-out-from-timeline timeline)
        ;; extracting users from favorites tweets
        ;;favorites (favorites-list :oauth-creds creds 
        ;;                          :params params)
        ;;people-favorites (map :screen_name (map :user (favorites :body)))


;
; SEARCH
;
(defn search-one [q since_id]
  (warn "Retreiving tweets for q =" q "and since_id =" since_id)
  (def results ((search :oauth-creds creds
                        :proxy (config :proxy)
                        :params {:q q
                                 :since_id since_id
                                 :count 1500}) :body))
  (warn "Found" (count (results :statuses)) " tweets for q =" q)
  results)


(defn search-all [q since_id]
  (def results {})
  (def statuses [])
  (def tot 100)
  (def new_since_id since_id)
  (while (== tot 100)
     (do 
       (def results (search-one q new_since_id))
       (def new_since_id ((results :search_metadata) :max_id))
       (warn "since_id =" new_since_id)
       (def tot (count (results :statuses)))
       (def statuses (concat statuses (results :statuses)))
       (warn "tot results =" (count statuses))))
  {:search_metadata (results :search_metadata) :statuses statuses})
  
;
; CRAWLER
;

(defn crawl-twitter-users [ & {:keys [user-id depth crawler-params crawled-users]}]
  (warn "Starting crawling user-id" user-id "with depth =" depth ", crawled-users =" (count crawled-users))

  ;;
  ;; friends
  ;; 
  (def friends (get-user-friends :twitter-params {:user-id user-id}
                                 :crawler-params crawler-params))

  (sleep (crawler-params :sleep))

  ;;
  ;; followers
  ;; 
  (def followers (get-user-followers :twitter-params {:user-id user-id}
                                 :crawler-params crawler-params))

  (sleep (crawler-params :sleep))

  (def new-people (difference 
                   (union #{user-id} (set friends) (set followers))
                   crawled-users))

  (warn "New users found =" (count new-people) )

  (when (re-find #"stdout" (crawler-params :output))
    (doall (map #(println "user" %) new-people)))

  ;;
  ;; timeline
  ;; 
  (when (crawler-params :timelines)
    (let [timeline (get-user-timeline {:user-id user-id})]

      (when (re-find #"stdout" (crawler-params :output))
        (doall (map #(println "timeline" user-id %) timeline)))

      (when (re-find #"redis" (crawler-params :output))
        (save-key-value-hash source-name "user" user-id "timeline" timeline))

      (sleep (crawler-params :sleep))))
  
  (def new-crawled-users (union crawled-users #{user-id}))

  (if (> depth 0)
    (let [new-depth (- depth 1)]
      ;; TODO update new-crawled-users
      (doall (map #(crawl-twitter-users 
                    :user-id % 
                    :depth new-depth 
                    :crawler-params crawler-params 
                    :crawled-users new-crawled-users) 
                  new-people)))
    ;; else
    (info "Stopping crawling for userid =" user-id " depth=0"))
  new-crawled-users)
    
;; es. 21931460
(defn all-lists-members [list-id]
  (def cursor (atom -1))
  (def users (atom ()))
  (while (not= @cursor 0)
    (do ;;(println @cursor)
        (def l (lists-members :oauth-creds creds
                              :proxy (config :proxy)
                              :params {:cursor @cursor
                                       :list-id list-id}))
        ;;(reset! users (concat @users (map :screen_name  ((l :body) :users))))
        (reset! users (concat @users ((l :body) :users)))
        (reset! cursor ((l :body) :next_cursor))
        ))
  @users)
