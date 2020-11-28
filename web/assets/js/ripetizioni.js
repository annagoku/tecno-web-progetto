var SERVERURL = window.location.href;

var app =  new Vue({
    el: "#app",
    data : {
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
        this.setScrollingLogic();
        this.getSessionInfo();


    },
    methods: {
        setScrollingLogic : function () {
            var scroll_start = 0;
            var startchange = $('#start-change');
            var offset = startchange.offset();
            var teachers = $('#teachers');
            var offsetT = teachers.offset();

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
        getSessionInfo: function () {
            var self = this;
            $.get(SERVERURL + 'private/userlog', function (data) {
                //se ok
                self.user = data.user;
                console.log("GetSessionInfo -> " + JSON.stringify(data));
                console.log("sessionID -> "+data.sessionId);

                $.ajaxSetup({
                    headers:{
                        'JSESSIONID': data.sessionId
                    }
                });

                self.getCourses();
                self.getTeachers();

            }).fail(function () {
                //se errore
                console.log("No sessione utente");
            });
        },
        getCourses: function () {
            var self = this;

            $.get(SERVERURL+'public/courses?filter=home', function (data) {
                //se ok
                self.courses = data;

                console.log("GetCourses -> " + JSON.stringify(data));

            }).fail(function (xhr) {
                //se errore
                alert("Errore caricamento corsi -> status " + xhr.status);
            });
        },
        encodeURL: function (url) {
            if(this.sessionId != null) {
                return url+";"+this.sessionId;
            }
            else {
                return url;
            }
        },
        getTeachers: function () {
            var self = this;
            $.get(SERVERURL+'public/teachers?filter=home', function (data) {
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
            $.post(SERVERURL+'public/login', {user:this.login.username, psw:this.login.password}, function (data) {
                console.log("CheckLogin -> " + JSON.stringify(data));
                //se ok
                if(!data.result)
                    self.login.info=data;
                else {
                    document.location.href= SERVERURL+"private/";

                }

            }).fail(function (xhr) {
                console.log("CheckLogin error code "+xhr.status);
                console.log("CheckLogin error text "+xhr.responseText);
                self.login.info=JSON.parse(xhr.responseText);
                console.log(self.login.info);
            });
        },
        goToPrivateArea : function () {
            document.location.href= SERVERURL+"private/";
        },
        logoutAction: function () {
            var self = this;
            $.get(SERVERURL + 'private/logout', function (data) {
                //se ok
                console.log("Logout -> " + JSON.stringify(data));
                self.user = null;
            }).fail(function (xhr) {
                alert("Errore sul logout -> status " + xhr.status);
            });
        },
        showCatalog: function () {
            this.modalCatalog.errorMessage = null;
            this.modalCatalog.catalog = [];
            var self = this;
            $.get(SERVERURL + 'private/catalog', function (data) {
                //se ok
                self.modalCatalog.catalog = data;
                console.log("Lessons catalog -> " + JSON.stringify(data));
                $("#modalCatalog").modal('show');
            }).fail(function () {
                //se errore
                self.modalCatalog.errorMessage = "Impossibile reperire i dati, riprovare più tardi";
                $("#modalCatalog").modal('show');
            });
        }
    }
});


