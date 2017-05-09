(ns tasks.debug)

(defonce debug?
  ^boolean js/goog.DEBUG)

(defn spy
  ([label]
   (when debug?
     (.log js/console label)))
  ([label data]
   (when debug?
     (.log js/console label data))
   data))
