(ns cjbot.db
  (:gen-class)
  (:use 
   [cjbot.redis]
   [cjbot.mq_utils]
   )
  (:require 
   [clojure.data.json :as json]
   [taoensso.carmine :as car :refer (wcar)]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))


  
