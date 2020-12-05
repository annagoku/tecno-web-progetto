var SERVERURL = window.location.href.substr(0,window.location.href.indexOf(";jsessionid"));

// per fare in modo che tutte le richieste verso il server dichiarino di volere dei JSON
// sul controller posso quindi distingere le chiamate AJAX da quelle delle risorse HTML
$.ajaxSetup({
    headers:{
        "Accept": "application/json"
    }
});


var app =  new Vue({
    el: "#app",
    data : {
        loading: false,
        user: null,
        sessionId: null,
        courses: [],
        teachers: [],
        login : {
            username : "",
            password : "",
            info:null
        },
        modalCatalog: {
            catalog: [],
            errorMessage : null
        }
    },
    mounted : function () {
        this.sessionId = this.getSessionId();
        this.setScrollingLogic();
        this.getSessionInfo();


    },

    methods: {
        getSessionId: function () {
            if(window.location.href.indexOf("=") > 0)
                return window.location.href.substr(window.location.href.indexOf("=")+1, window.location.href.length);
            return null;
        },
        //opacità del menù principale a seconda della trasparenza
        setScrollingLogic : function () {
            var scroll_start = 0;
            var startchange = $('#start-change');
            var offset = startchange.offset();
            var teachers = $('#teachers');
            var offsetT = teachers.offset();
        //si attiva al resize della finestra
            $(window).resize(function(){
                startchange = $('#start-change');
                offset = startchange.offset();
                teachers = $('#teachers');
                offsetT = teachers.offset();
            });

            $(window).scroll(function(){
                scroll_start = $(document).scrollTop();
                if(scroll_start > offset.top-130 && scroll_start < offsetT.top-50) {
                    $("#navbar").addClass("navbar-opaque");
                    $("#navlink-home").removeClass("active");
                    $("#navlink-teachers").removeClass("active");
                    $("#navlink-courses").addClass("active");

                }
                else if(scroll_start >= offsetT.top-50) {
                    $("#navlink-home").removeClass("active");
                    $("#navlink-teachers").addClass("active");
                    $("#navlink-courses").removeClass("active");
                }
                else{
                    $("#navbar").removeClass("navbar-opaque");
                    $("#navlink-home").addClass("active");
                    $("#navlink-teachers").removeClass("active");
                    $("#navlink-courses").removeClass("active");
                }
            })
        },
        invalidateSession: function () {
            this.sessionId = null;
            this.user = null;
        },
        getSessionInfo: function () {
            var self = this;

            $.get(this.encodeURL(SERVERURL + 'private/userlog', ""), function (data) {
                //se ok
                self.user = data.user;
                console.log("GetSessionInfo -> " + JSON.stringify(data));
                console.log("sessionID -> "+data.sessionId);
                
                self.getCourses();
                self.getTeachers();

            }).fail(function () {
                //se errore
                console.log("No sessione utente");
                self.invalidateSession();
            });
        },
        getCourses: function () {
            var self = this;

            $.get(this.encodeURL(SERVERURL+"public/courses","?filter=home"), function (data) {
                //se ok
                self.courses = data;
                console.log("GetCourses -> " + JSON.stringify(data));
                
            }).fail(function (xhr) {
                //se errore
                alert("Errore caricamento corsi -> status " + xhr.status);
                
            });
        },
        encodeURL: function (url, queryString) {
            if(this.sessionId != null) {
                return url+";jsessionid="+this.sessionId+(queryString != null ? queryString : "");
            }
            else {
                return url +(queryString != null ? queryString : "");
            }
        },
        getTeachers: function () {
            var self = this;
            $.get(this.encodeURL(SERVERURL+'public/teachers','?filter=home'), function (data) {
                //se ok
                
                self.teachers = data;
                console.log("GetTeachers -> " + JSON.stringify(data));
            }).fail(function (xhr) {
                //se errore
                
                alert("Si è verificato un errore ->" + xhr.status);
            });
        },
        loginaction: function (e) {
            e.preventDefault();
            var self=this;
            this.login.info = null;
            self.loading = true;
            $.post(this.encodeURL(SERVERURL+'public/login',''), {user:this.login.username, psw:this.login.password}, function (data) {
                console.log("CheckLogin -> " + JSON.stringify(data));
                //se ok
                self.loading = false;
                if(!data.result)
                    self.login.info=data;
                else {

                    self.sessionId = data.sessionId;
                    document.location.href= self.encodeURL(SERVERURL+"private/");
                }

            }).fail(function (xhr) {
                self.loading = false;
                console.log("CheckLogin error code "+xhr.status);
                console.log("CheckLogin error text "+xhr.responseText);
                self.login.info=JSON.parse(xhr.responseText);
                console.log(self.login.info);
            });
        },
        goToPrivateArea : function () {
            document.location.href= this.encodeURL(SERVERURL+"private/");
        },
        logoutAction: function () {
            var self = this;
            self.loading = true;
            $.get(this.encodeURL(SERVERURL + 'private/logout'), function (data) {
                //se ok
                self.loading = false;
                console.log("Logout -> " + JSON.stringify(data));
                self.user = null;
                self.invalidateSession();
            }).fail(function (xhr) {
                self.loading = false;
                alert("Errore sul logout -> status " + xhr.status);
            });
        },
        showCatalog: function () {
            this.modalCatalog.errorMessage = null;
            this.modalCatalog.catalog = [];
            var self = this;
            self.loading = true;
            $.get(this.encodeURL(SERVERURL + 'public/catalog',''), function (data) {
                //se ok
                self.loading = false;
                self.modalCatalog.catalog = data;
                
                console.log("Lessons catalog -> " + JSON.stringify(data));
                $("#modalCatalog").modal('show');
            }).fail(function () {
                //se errore
                self.loading = false;
                self.modalCatalog.errorMessage = "Impossibile reperire i dati, riprovare più tardi";
                $("#modalCatalog").modal('show');
            });
        }
    }
});


