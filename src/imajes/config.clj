(ns imajes.config)

(def creds {:access-key (System/getenv "AWSKEY")
            :secret-key (System/getenv "AWSSECRET")})
(def bucket (System/getenv "AWSBUCKET"))
(def image-size 400)
(def thumb-stamp (format "_THUMB%d" image-size))
(def image-key-list (ref '()))