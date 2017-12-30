(ns tiltontec.tag.style
  (:require
    [tiltontec.util.core :refer [pln]]
    [tiltontec.cell.base :refer [md-ref? ia-type unbound]]
    [tiltontec.cell.observer :refer [observe observe-by-type]]
    [tiltontec.cell.evaluate :refer [not-to-be not-to-be-self]]
    [tiltontec.model.core
     :refer-macros [the-kids mdv!]
     :refer [md-get fasc fm! make md-reset! backdoor-reset!]
     :as md]
    [goog.dom.classlist :as classlist]))


(defn make-css-inline [tag & stylings]
  (prn :make-css stylings)
  (let [c (apply make
                 :type :tiltontec.tag.css/css
                 :tag-id (when tag (:id @tag))
                 :css-keys (for [[k _] (partition 2 stylings)] k)
                 stylings)]

    c))

#_ (case slot
     :style (set! (.-style dom) newv)
     :hidden (set! (.-hidden dom) newv)
     :class (classlist/set dom newv)
     :checked (set! (.-checked dom) newv)
     )

#_ (case slot
     :display (set! (.-display (.-style dom)) newv))

(defmethod observe-by-type [:tiltontec.tag.css/css] [slot me newv oldv _]
  (when (not= oldv unbound)
    (when-let [dom (tag-dom me)]


      (cond
        (= slot :content) (set! (.-innerHTML dom) newv)

        (= slot :style) (set! (.-style dom) newv)

        (+global-attr+ slot)
        (case slot
          :hidden (set! (.-hidden dom) newv)
          :class (classlist/set dom newv)
          :checked (set! (.-checked dom) newv)
          )

        (+inline-css+ slot)
        (do                                                 ;; (println :obs-inline-css!!! slot)
          (case slot
            :display (set! (.-display (.-style dom)) newv)))))))

#_ @(make-css-inline nil :a (c-in 1) :b (c? (* (md-get me :a))))