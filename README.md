# imajes

Clojure image manager

## Install

import the library into your project.clj file:

    [imajes "0.1"]

## Example

Put your aws secret key into an environment variable called AWSSECRET and public key into AWSKEY. Also write the bucket name to an environment variable called BUCKET
```shell
export AWSKEY=[your key]
export AWSSECRET=[your secret key]
export BUCKET=[intended bucket]
```

Also make sure the bucket exists. ([#1](https://github.com/pierrel/imajes/issues/1))

Place a bunch of images in your bucket.

```clojure
(ns my-app
    (:use imajes.core :only [generate-thumbnails image-urls-map reset-image-key-list]))
(generate-thumbnails)  ; will create a thumbnail for each image

(image-urls-map)       ; will return a seq of maps that look like:
                       ; :image     - the original image
                       ; :thumbnail - the generated thumbnail
                       ; :key       - the s3 key (without an extension separator, i.e. IMG_123JPG) 

(reset-image-key-list) ; will reset the cached version of image-urls-map
```