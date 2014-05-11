(ns cjbot.sources.twitter
  (:use
   [clojure.set]
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.utils]
   [twitter.request]
   [twitter.api.restful]
   [twitter.api.search]
   [carica.core]
   ;; my files
   [cjbot.mq_utils]
   [cjbot.redis]
   [cjbot.sources.twitter_utils]
   )
  (:require 
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)])
  )

;; -----------------------------------------
;; twitter-followers-id
;; -----------------------------------------

(defn twitter-followers-ids [req-value]
  (let [user-id (req-value :user-id)
        cursor (atom -1)
        users (atom ())
        queue "db.update.req"]
    (while (not= @cursor 0)
      (do ;;(println @cursor)
        (let [twitter-params {:user-id user-id :cursor @cursor}
              l (followers-ids :oauth-creds creds
                               :proxy (config :proxy)
                               :params twitter-params)
              new-users ((l :body) :ids)]
          (reset! users (concat @users new-users))
          (reset! cursor ((l :body) :next_cursor))
          (warn "Sending" (count @users) "followers-ids for user" user-id "to queue=" queue)
          (debug "users:" @users) 
          (cjbot-mq-publish queue
                      {:source "twitter" 
                       :object "user" 
                       :id  user-id
                       :attribute {:followers @users}}))))
    @users))


;; -----------------------------------------
;; twitter-followers-list
;; -----------------------------------------

(defn twitter-followers-list [req-value]
  (let [user-id (req-value :user-id)
        cursor (atom -1)
        users (atom ())
        queue "db.update.req"]
    (while (not= @cursor 0)
      (do ;;(println @cursor)
        (let [twitter-params {:user-id user-id :cursor @cursor}
              l (followers-list :oauth-creds creds
                                :proxy (config :proxy)
                                :params twitter-params)
              new-users ((l :body) :users)]
          (reset! users (concat @users new-users))
          (reset! cursor ((l :body) :next_cursor))
          (warn "Sending" (count new-users) "new users to queue=" queue)
          ;; extract users to be saved to db
          (doall (map #(cjbot-mq-publish queue
                                         {:source "twitter" 
                                          :object "user" 
                                          :id (% :id) 
                                          :attribute {:lookup %}})
                      new-users))      
          ))) ;; end while
    (warn "Sending" (count @users) "followers to queue=" queue)
    (cjbot-mq-publish queue
                      {:source "twitter" 
                       :object "user" 
                       :id  user-id
                       :attribute {:followers (map :id @users)}})
    @users))
;; -----------------------------------------
;; twitter-friends-id
;; -----------------------------------------

(defn twitter-friends-ids [req-value]
  (let [user-id (req-value :user-id)
        cursor (atom -1)
        users (atom ())
        queue "db.update.req"]
    (while (not= @cursor 0)
      (do ;;(println @cursor)
        (let [twitter-params {:user-id user-id :cursor @cursor}
              l (friends-ids :oauth-creds creds
                               :proxy (config :proxy)
                               :params twitter-params)
              new-users ((l :body) :ids)]
          (reset! users (concat @users new-users))
          (reset! cursor ((l :body) :next_cursor))
          (warn "Sending" (count @users) "friends-ids for user" user-id "to queue=" queue)
          (debug "users:" @users) 
          (cjbot-mq-publish queue
                      {:source "twitter" 
                       :object "user" 
                       :id  user-id
                       :attribute {:friends @users}}))))
    @users))

;; -----------------------------------------
;; twitter-friends-list
;; -----------------------------------------

(defn twitter-friends-list [req-value]
  (let [user-id (req-value :user-id)
        cursor (atom -1)
        users (atom ())
        queue "db.update.req"]
    (while (not= @cursor 0)
      (do ;;(println @cursor)
        (let [twitter-params {:user-id user-id :cursor @cursor}
              l (friends-list :oauth-creds creds
                              :proxy (config :proxy)
                              :params twitter-params)
              new-users ((l :body) :users)]
          (reset! users (concat @users new-users))
          (reset! cursor ((l :body) :next_cursor))
          (warn "Sending" (count new-users) "new users to queue=" queue)
          ;; extract users to be saved to db
          (doall (map #(cjbot-mq-publish queue
                                         {:source "twitter" 
                                          :object "user" 
                                          :id (% :id) 
                                          :attribute {:lookup %}})
                      new-users))      
          ))) ;; end while
    (warn "Sending" (count @users) "friend list to queue=" queue)
    (cjbot-mq-publish queue
                      {:source "twitter" 
                       :object "user" 
                       :id  user-id
                       :attribute {:friends (map :id @users)}})
    @users))

;; -----------------------------------------
;; twitter-lists-members
;; -----------------------------------------

;; es. 21931460
(defn twitter-lists-members [req-value]
  (let [list-id (req-value :list-id)
        cursor (atom -1)
        users (atom ())
        queue "db.update.req"]
    (while (not= @cursor 0)
      (do ;;(println @cursor)
        (let [l (lists-members :oauth-creds creds
                              :proxy (config :proxy)
                              :params {:cursor @cursor
                                       :list-id list-id})
              new-users ((l :body) :users)]
          (reset! users (concat @users new-users))
          (reset! cursor ((l :body) :next_cursor))
          (warn "Sending" (count new-users) "new users to queue=" queue)
          ;; extract users to be saved to db
          (doall (map #(cjbot-mq-publish queue
                                         {:source "twitter" 
                                          :object "user" 
                                          :id (% :id) 
                                          :attribute {:lookup %}})
                      new-users))      
          ))) ;; end while
    (warn "Sending" (count @users) "list members to queue=" queue)
    (cjbot-mq-publish queue
                      {:source "twitter" 
                       :object "list" 
                       :id list-id 
                       :attribute {:members (map :id @users)}})
    @users))

;; -----------------------------------------
;; twitter-users-suggestions-slug
;; -----------------------------------------

(defn twitter-users-suggestions-slug [req-value]
  (let [slug (req-value :slug)
        queue "db.update.req"
        req (users-suggestions-slug-members :oauth-creds creds 
                                            :params {:slug slug})
        users (req :body)]
    (warn "Sending" (count users) "users to queue" queue)
    ;; extract users to be saved to db
    (doall (map #(cjbot-mq-publish queue
                                   {:source "twitter" 
                                    :object "user" 
                                    :id (% :id) 
                                    :attribute {:lookup %}})
                users))  
    
    (warn "Sending users_suggestions_slug" slug "to queue=" queue)
    (cjbot-mq-publish queue
                      {:source "twitter" 
                       :object "users_suggestions_slug" 
                       :id slug
                       :attribute {:members (map :id users)}})
    users))

;; -----------------------------------------
;; twitter-users-lookup
;; -----------------------------------------

(defn twitter-users-lookup [req-value]
  (let [user-id (req-value :user-id)
        queue "db.update.req"
        req (users-lookup :oauth-creds creds
                               :proxy (config :proxy)
                               :params {:user-id user-id})
        users (req :body)]
    (debug users)
    (warn "Sending" (count users) "users to queue" queue)
    ;; extract users to be saved to db
    (doall (map #(cjbot-mq-publish queue
                                   {:source "twitter" 
                                    :object "user" 
                                    :id (% :id) 
                                    :attribute {:lookup %}})
                users))  
    users))

;; -----------------------------------------
;; twitter-users-suggestions
;; -----------------------------------------

(defn twitter-users-suggestions [req-value]
  (let [lang (req-value :lang)
        queue "db.update.req"
        req (users-suggestions :oauth-creds creds
                               :proxy (config :proxy)
                               :lang lang)
        suggestions (req :body)]
    (warn "Exploring" (count suggestions) "suggestions..." suggestions)
    ;; extract users to be saved to db
    (doall (map #(twitter-users-suggestions-slug {:slug (% :slug)})
                suggestions))
    suggestions))
