(ns cjbot.mongodb
  (:gen-class)
  (:use [carica.core])
  (:import [com.mongodb MongoOptions ServerAddress])
  (:require 
   [monger.core :as mg]
   [clojure.data.json :as json]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))



(let [^MongoOptions opts (mg/mongo-options :threads-allowed-to-block-for-connection-multiplier 300)
      ^ServerAddress sa  (mg/server-address (config :redis :host) (config :redis :port))]
  (mg/connect! sa opts))
