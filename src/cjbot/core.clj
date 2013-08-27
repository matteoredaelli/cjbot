(ns cjbot.core
  (:gen-class)
  (:use [cjbot.sources.twitter]
        ;;[carica.core]
        ))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (crawl-twitter-users :depth 0 
                       :user-id 16837493 ;; "matteoredaelli"
                       :crawler-params {;; TODO :crawl-friends true 
                                        ;; TODO :crawl-followers true 
                                        ;; TODO :crawl-urls false
                                        :crawled-users #{}
                                        ;; TODO :redis-server {:server "localhost" :port 1111}
                                        ;; TODO :amqp-server {:server "localhost" :port 1111 :queue "cjbot.results"}
                                        ;; TODO :riak-server {:server "localhost" :port 1111}
                                        }
                       ))
