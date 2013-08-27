(ns cjbot.core
  (:gen-class)
  (:use [cjbot.sources.twitter]
        ;;[carica.core]
        ))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (crawl-twitter-users :depth 1 
                      :twitter-params {:screen-name "matteoredaelli"}
                      :crawler-params {:crawl-friends true 
                                       :crawl-followers true 
                                       :crawl-urls false
                                       :crawled-users #{}
                                        
                                       :redis-server {:server "localhost" :port 1111}
                                       ;;:amqp-server {:server "localhost" :port 1111 :queue "cjbot.results"}
                                       ;;:riak-server {:server "localhost" :port 1111}
                                       }
                      ))
