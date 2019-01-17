;
(function () {
    'use strict';
    var Editor = function Editor(element) {
        this.element = element;
        this.init();
    };
    Editor.prototype.Constants = {
        address: "",
    };
    Editor.prototype.init = function () {
        if (this.element) {
            // https://github.com/sparksuite/simplemde-markdown-editor
            this.mde = new SimpleMDE({
                element: this.element,
                hideIcons: ["guide"]
            });

            window.onhashchange = this.onHashChange.bind(this);
        }
    };

    Editor.prototype.onFetch = function (hash) {
        fetch("/api/get/" + hash, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                }
            })
            .then(response => response.json())
            .then(data => {
                let value = JSON.stringify(data)['content'];
                this.mde.value(value);
            });
    }
    Editor.prototype.onHashChange = function (e) {
        var hash = window.location.hash;
        this.onFetch(hash.substring(1));
        console.log(e);
    };
    window["Editor"] = Editor;

    new Editor(document.getElementById("edit-text"));
})();