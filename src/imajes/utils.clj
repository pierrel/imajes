(ns imajes.utils
  (:use [imajes.config]))

(defn key-to-url [key]
  (format "http://s3.amazonaws.com/%s/%s" bucket key))

(defn thumb-key-to-original-key
  [thumb-key]
  (clojure.string/replace thumb-key thumb-stamp ""))

(defn original-key-to-thumb-key
  [original-key]
  (clojure.string/replace original-key "." (format "%s." thumb-stamp)))

(defn thumb?
  [s3-object]
  (re-find (re-pattern thumb-stamp) (:key s3-object)))

(defn original?
  [s3-object]
  (not (re-find #"_THUMB" (:key s3-object))))

(defn split-thumbs-and-originals
  "Given a set of objects will return a map:
   :thumbs - a seq of thumbnail image objects
   :originals - a seq of original image object"
  [objects]
  (group-by (fn [object] (cond
                          (original? object) :originals
                          (thumb? object) :thumbs
                          :else :other))
            objects))

(defn map-original-with-thumb
  "Given a seq of thumb objects will return a map of original image keys to image object"
  [thumbs]
  (group-by (fn [thumb-object] (thumb-key-to-original-key (:key thumb-object))) 
            thumbs))

