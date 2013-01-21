(ns imajes.storage
  (:require [aws.sdk.s3 :as s3])
  (:use [imajes.utils]
        [imajes.rendering]))

(defn put-image-in-s3
  [image filename]
  (do
    (s3/put-object creds bucket filename (image-to-input-stream image))
    (s3/update-object-acl creds bucket filename (s3/grant :all-users :read))));; setting acl from put didn't seem to work...

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

(defn usable-keys-from-s3
  "Returns a seq of keys of original images that have a thumbnail"
  []
  (map (fn [object] (:key object))
       (images-with-thumbnails)))

