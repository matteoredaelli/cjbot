(ns cjbot.sources.twitter
  (:use
   [clojure.set]
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful]
   [carica.core])
  (:require 
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)])
  )

(def creds (make-oauth-creds (config :sources :twitter :creds :app-consumer-key)
                             (config :sources :twitter :creds :app-consumer-secret)
                             (config :sources :twitter :creds :user-access-token) 
                             (config :sources :twitter :creds :user-access-token-secret)))

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

(defn extract-twitter-user [ & {:keys [twitter-params]}]
  ;; extracting info about the user params must be
  ;; {:screen-name user} or {:user-id user}
  (warn "Starting extract-twitter-user user_id =" (twitter-params :user-id) "screen_name =" (twitter-params :screen-name))
  (let [
      
        user-show (users-show :oauth-creds creds 
                              :proxy (config :proxy)
                              :params twitter-params)
     
        timeline (statuses-user-timeline :oauth-creds creds 
                                         :proxy (config :proxy)
                                         :params (merge twitter-params 
                                                        {:include-rts true}))

        ;;people-retwitting (get-retwitters-out-from-timeline timeline)
        ;;mentions-names-out (get-mentions-out-from-timeline timeline)
        ;; extracting users from favorites tweets
        ;;favorites (favorites-list :oauth-creds creds 
        ;;                          :params params)
        ;;people-favorites (map :screen_name (map :user (favorites :body)))

   
        friends (((friends-ids :oauth-creds creds 
                               :proxy (config :proxy)
                               :params (merge twitter-params {:count 5000}))
                  :body) 
                 :ids)
 
        followers (((followers-ids :oauth-creds creds 
                                   :proxy (config :proxy)
                                   :params (merge twitter-params {:count 5000}))
                    :body) 
                   :ids)
        ]
    (debug "friends-count =" (count friends) ((user-show :body) :friends_count))
    (debug "followers-count =" (count followers) ((user-show :body) :followers_count))
    (warn "Finished extract-twitter-user user_id =" (twitter-params :user-id) "screen_name =" (twitter-params :screen-name))

    {:user-show user-show
     :timeline timeline
     :friends friends
     :followers followers
     }
  )
)

(defn crawl-twitter-users [ & {:keys [user-id depth crawler-params crawled-users]}]
  (warn "Starting crawling user-id" user-id "with depth =" depth ", crawled-users =" (count crawled-users))
  (warn "Retreiving friends for user-id =" user-id)
  (def friends 
    (((friends-ids :oauth-creds creds 
                     :proxy (config :proxy)
                     :params {:user-id user-id
                              :count 5000})
      :body)
     :ids))
  (warn "Found" (count friends) "friends for user-id " user-id)
  (sleep (crawler-params :sleep))


  (warn "retreiving followers for user-id =" user-id) 
  (def followers 
    (((followers-ids :oauth-creds creds 
                     :proxy (config :proxy)
                     :params {:user-id user-id
                              :count 5000})
      :body)
     :ids))
  (warn "Found" (count followers) "followers for user-id " user-id)
  (sleep (crawler-params :sleep))

  (def new-people (difference 
                   (union #{user-id} (set friends) (set followers))
                   crawled-users))

  (warn "New users found =" (count new-people) )
  (doall (map #(println "user" %) new-people))
  (doall (map #(println "friend" user-id %) friends))
  (doall (map #(println "follower" user-id %) followers))

  (sleep (crawler-params :sleep))

  (if (> depth 0)
    (let [new-depth (- depth 1)
          new-crawled-users (union crawled-users #{user-id})]
      (doall (map #(crawl-twitter-users 
                    :user-id % 
                    :depth new-depth 
                    :crawler-params crawler-params 
                    :crawled-users new-crawled-users) 
                  new-people)))
    ;; else
    (info "Stopping crawling for userid =" user-id " depth=0")))
    
