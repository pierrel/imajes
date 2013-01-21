(ns imajes.core
  (:use [imajes.config]
        [imajes.rendering]
        [imajes.utils]
        [imajes.storage]))

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

(defn generate-thumbnails
 []
 (for [original (images-without-thumbnails)]
   (let [key (:key original)
         url (key-to-url key)
         icon (iconize-image-from-url url)]
     (if icon (put-image-in-s3 icon (original-key-to-thumb-key key)))
     [key icon])))