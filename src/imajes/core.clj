(ns imajes.core
  (:require [aws.sdk.s3 :as s3])
  (:use [imajes.config])
  (:import (javax.swing ImageIcon)
           (javax.imageio ImageIO)
           (java.io File ByteArrayOutputStream ByteArrayInputStream)
           (java.awt.image BufferedImage)
           (java.net URL)
           (java.awt Image)
           (java.awt RenderingHints)))

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

(declare images-with-thumbnails)

(defn usable-keys-from-s3
  "Returns a seq of keys of original images that have a thumbnail"
  []
  (map (fn [object] (:key object))
       (images-with-thumbnails)))

(defn image-urls-map
  "returns a seq of maps with the following keys:
   :image - the url to the original image
   :thumb - the url to the thumnail version of the image
   :key   - the key of the object without the '.'"
  []
  (if (empty? @image-key-list)
    (dosync (ref-set image-key-list (usable-keys-from-s3))))
  (map (fn [image-key]
         {:image (key-to-url image-key)
          :thumbnail (key-to-url (original-key-to-thumb-key image-key))
          :key (clojure.string/replace image-key "." "")})
       @image-key-list))

(defn reset-image-key-list
  "blanks cache of the usable objects hash"
  []
  (dosync (ref-set image-key-list '())))

;; Processing stuff

(defn get-buffered-image
  "Transforms a java Image object to a BufferedImage object"
  [image width height]
  (let [bufferedImage (new BufferedImage width height (. BufferedImage TYPE_INT_RGB))
        gContext (. bufferedImage createGraphics)]
    (. gContext setRenderingHint (. RenderingHints KEY_INTERPOLATION) (. RenderingHints VALUE_INTERPOLATION_BILINEAR))
    (. gContext setRenderingHint (. RenderingHints KEY_RENDERING) (. RenderingHints VALUE_RENDER_QUALITY))
    (. gContext setRenderingHint (. RenderingHints KEY_ANTIALIASING) (. RenderingHints VALUE_ANTIALIAS_ON))
    (let [result (. gContext drawImage image 0 0 width height nil)]
      (. gContext dispose)
      (if result bufferedImage nil))))

(defn image-from-url
  [url]
  (.getImage (new ImageIcon (new URL url))))

(defn iconize-image-from-url
  [url]
  (let [image (image-from-url url)
        scale (/ (. image getWidth) (. image getHeight))]
    (get-buffered-image image image-size (/ image-size scale))))

(defn image-to-input-stream
  [image]
  (let [os (new ByteArrayOutputStream)]
    (. ImageIO write image "jpg" os)
    (new ByteArrayInputStream (. os toByteArray))))

(defn put-image-in-s3
  [image filename]
  (do
    (s3/put-object creds bucket filename (image-to-input-stream image))
    (s3/update-object-acl creds bucket filename (s3/grant :all-users :read))));; setting acl from put didn't seem to work...

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

(defn images-without-thumbnails
  []
  (let [objects (:objects (s3/list-objects creds bucket))
        thumb-original-map (split-thumbs-and-originals objects)
        thumbs (:thumbs thumb-original-map)
        originals (:originals thumb-original-map)
        original-with-thumb-map (map-original-with-thumb thumbs)]
    (filter (fn [object] (not (contains? original-with-thumb-map (:key object))))
            originals)))

(defn images-with-thumbnails
  []
  (let [objects (:objects (s3/list-objects creds bucket))
        thumb-original-map (split-thumbs-and-originals objects)
        thumbs (:thumbs thumb-original-map)
        originals (:originals thumb-original-map)
        original-with-thumb-map (map-original-with-thumb thumbs)]
    (filter (fn [object] (contains? original-with-thumb-map (:key object)))
            originals)))

(defn generate-thumbnails
 []
 (for [original (images-without-thumbnails)]
   (let [key (:key original)
         url (key-to-url key)
         icon (iconize-image-from-url url)]
     (if icon (put-image-in-s3 icon (original-key-to-thumb-key key)))
     [key icon])))