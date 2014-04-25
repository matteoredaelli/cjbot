(ns cjbot.mq
  (:gen-class)
  (:use 
   [cjbot.redis]
   [cjbot.mq_utils]
   [cjbot.crawler]
   [cjbot.db]
   ;; sources
   [cjbot.sources.twitter])
  (:require 
   [clojure.data.json :as json]
   [taoensso.carmine :as car :refer (wcar)]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(def listener-crawler
  (let [queue-crawler-req (cjbot-queue-name-with-prefix "crawler.req")
        queues-crawler (cjbot-queue-name-with-prefix "crawler*")
        queue-db-update-req (cjbot-queue-name-with-prefix "db.update.req")
        queues-db (cjbot-queue-name-with-prefix "db*")]
    (car/with-new-pubsub-listener (:spec redis-conn)
      ;; crawler
      {queue-crawler-req (fn f1 [msg] (cjbot-crawler-reqs-listener msg))
       ;;"cjbot.crawler.req"   (fn f2 [msg] (println "Pattern match: " (= (first msg) "message")))
       queues-crawler (fn f3 [msg] (println "Pattern match: " msg))
      ;; db
       queue-db-update-req (fn f2 [msg] (println "Pattern match: " msg))
       queues-db (fn f3 [msg] (println "Pattern match: " msg))}

      ;; crawler
      (car/subscribe  queue-crawler-req)
      (car/psubscribe queues-crawler)
      ;; DB
      (car/subscribe  queue-db-update-req)
      (car/psubscribe queues-db)
      )))
