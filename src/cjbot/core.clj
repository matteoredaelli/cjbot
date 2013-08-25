(ns cjbot.core
  (:gen-class)
  (:use [cjbot.sources.twitter]
        ;;[carica.core]
        ))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (crawl-twitter-user :depth 1 
                      :twitter-params {:screen-name "matteoredaelli"}
                      :crawler-params {:crawl-friends true 
                                       :crawl-followers true 
                                       :crawl-urls true
                                       :results-to-redis true
                                       :redis {:server "localhost" :port 1111}
                                       :amqp {:server "localhost" :port 1111 :queue}
                                       }
                      )) 
