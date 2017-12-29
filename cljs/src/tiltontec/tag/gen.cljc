(ns tiltontec.tag.gen
  (:require
    #?(:cljs [goog.dom.forms :as form])
    #?(:clj [clojure.pprint :refer :all]
       :cljs cljs.pprint :clj :refer [pprint cl-format])
            [tiltontec.cell.base :refer [md-ref? ia-type unbound]]
            [tiltontec.cell.evaluate :refer [not-to-be not-to-be-self]]
            [tiltontec.model.core :refer [make md-get] :as md]))

(def +tag-sid+ (atom -1))

(defn tag-init! []
  (reset! +tag-sid+ -1))

(def tag-by-id (atom {}))

(defn dom-tag [dom]
  (let [tag (get @tag-by-id (.-id dom))]
    (assert tag (str "dom-tag did not find js for id " (.-id dom)
                     " of dom " dom))
    tag))

(defn make-tag [tag attrs aux c?kids]
  (prn :make-tag tag attrs aux )
  (let [tag-id (str (or (:id attrs)
                        (swap! +tag-sid+ inc)))
        mx-tag (apply make
                      :type :tiltontec.tag.html/tag
                      :tag tag
                      :id tag-id
                      :attr-keys (remove #{:id} (keys attrs))
                      :kids c?kids
                      (concat (vec (apply concat (seq (dissoc attrs :id))))
                              (vec (apply concat (seq aux)))))]
    (swap! tag-by-id assoc tag-id mx-tag)
    mx-tag))


(defmethod not-to-be [:tiltontec.tag.html/tag] [me]
  ;; todo: worry about leaks
  (doseq [k (:kids @me)]
    (when (md-ref? k)
      (not-to-be k)))
  (swap! tag-by-id dissoc (md-get me :id))
  (not-to-be-self me))

(defmacro deftag [tag]
  (let [kids-sym (gensym "kids")
        tag-name (str tag)
        attrs-sym (gensym "attrs")]
    `(defmacro ~tag [~attrs-sym & ~kids-sym]
       `(tiltontec.tag.gen/make-tag ~~tag-name ~~attrs-sym
                                    (tiltontec.model.core/c?kids ~@~kids-sym)))))

(defmacro h1 [& vargs]
  (let [tag-name "h1"]
    (cond
      (nil? vargs)
      `(tiltontec.tag.gen/make-tag ~tag-name {} {} nil)

      (map? (first vargs))
      (cond
        (map? (second vargs))
        `(tiltontec.tag.gen/make-tag ~tag-name ~(first vargs) ~(second vargs)
                                     ~(when-let [kids (seq (nthrest vargs 2))]
                                        `(tiltontec.model.core/c?kids ~@kids)))

        :default `(tiltontec.tag.gen/make-tag
                        ~tag-name ~(first vargs) {}
                        ~(when-let [kids (seq (nthrest vargs 1))]
                           `(tiltontec.model.core/c?kids ~@kids))))

      :default `(tiltontec.tag.gen/make-tag
                  ~tag-name {} {}
                  (tiltontec.model.core/c?kids ~@vargs)))))

#_(macroexpand '(h1))
#_(macroexpand '(h1 {:cool 42}))
#_(macroexpand '(h1 "cool 42" "booya"))
#_(macroexpand '(h1 {:cool 42} {:cool 43}))
#_(macroexpand '(h1 {} "Hi mom"))
#_(macroexpand '(h1 {:cool 42} {:cell 7} "Hi mom" "yowza"))
(defmacro deftags [& tags]
  `(do ~@(for [tag tags]
           `(deftag ~tag))))

;;; This....
(declare section section label header footer input p span a img ul li div button)

;;; ...avoids mistaken/benign warnings from this:
(deftags section section label header footer input p span a img ul li div button)

;;; n.b. Above list of tags needs to be extended, or just use make-tag

;;; --- event conveniences -------------------

(defn evt-tag [e]
  (dom-tag (.-target e)))

#?(:cljs
   (defn target-value [evt]
     (form/getValue (.-target evt))))

