(ns cjbot.sources.twitter
  (:use
   [clojure.set]
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
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
;; crawler function
;; -----------------------------------------

;; es. 21931460
(defn twitter-lists-members [req-value]
  (let [list-id (req-value :list-id)
        cursor (atom -1)
        users (atom ())
        queue (cjbot-queue-name-with-prefix "db.update.req")]
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
                                          :key-name "id" 
                                          :key-val (% :id) 
                                          :attribute-name "lookup" 
                                          :attribute-val %})
                      new-users))
          ;;(wcar* (car/publish "cjbot.db.update.resp" resp-json))
          
          )))
  @users))
