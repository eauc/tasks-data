(ns tasks.debug)

(defonce debug?
  (not= "production" (aget js/process "env" "NODE_ENV")))

(defn spy
  ([label]
   (when debug?
     (.log js/console label)))
  ([label data]
   (when debug?
     (.log js/console label data))
   data))
