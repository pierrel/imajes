(ns imajes.rendering
  (:use [imajes.config])
  (:import (javax.swing ImageIcon)
           (javax.imageio ImageIO)
           (java.io File ByteArrayOutputStream ByteArrayInputStream)
           (java.awt.image BufferedImage)
           (java.net URL)
           (java.awt Image)
           (java.awt RenderingHints)))

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
  "Takes a url string and returns a BufferedImage with config width"
  [url]
  (let [image (image-from-url url)
        scale (/ (. image getWidth) (. image getHeight))]
    (get-buffered-image image image-size (/ image-size scale))))

(defn image-to-input-stream
  [image]
  (let [os (new ByteArrayOutputStream)]
    (. ImageIO write image "jpg" os)
    (new ByteArrayInputStream (. os toByteArray))))

