(ns cjbot.redis
  (:gen-class)
  (:use [carica.core])
  (:use [cjbot.mq_utils])
  (:require 
   [clojure.data.json :as json]
   [taoensso.carmine :as car :refer (wcar)]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(def redis-conn {:pool {} :spec {:host (config :redis :host)
                                 :port (config :redis :port)}})

(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn save-key-hash-value [key hash value]
  (warn "Saving REDIS key =" key)
  (wcar* (car/hsetnx key hash (encode-message value))))

(defn save-key-value-hash [category object id hash value]
  (let [key (format "%s:%s:%d" category object id)]
    (save-key-hash-value key hash value)
    (warn "Saving to REDIS key =" key "hash =" hash)))

(defn cjbot-mq-publish [subqueue msg]
  (let [msg-json (encode-message msg)
        queue (cjbot-queue-name-with-prefix subqueue)]
    (warn "MQ :: publishing to queue=" queue msg)
    (wcar* (car/publish queue msg-json))))

