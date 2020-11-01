//URL della pagina corrente
var SERVERURL = window.location.href;
var HOMEURL = SERVERURL.substr(0, SERVERURL.indexOf("private"));

window.userSession = null;

var weekDay= new Vue ({
    el:"#app",
    data: {
        user: null,
        days: [
            'Lunedì',
            'Martedì',
            'Mercoledì',
            'Giovedì',
            'Venerdì'
        ],
        hours: [{id: 1, startHour: "15:00"},{id: 2, startHour: "16:00"},{id: 3, startHour: "17:00"},{id: 4, startHour: "18:00"}],
        lessonsMatrix : null,
        modalCatalog: {
            catalog: [],
            errorMessage : null
        },
        modalLesson: {
            selectedLesson : null,
            wantConfirm: false,
            wantCancel: false,
            errorMessage: null,
        },
        modalNewReservation : {
            courses: [],
            matrix: null,
            errorMessage: null
        }

    },
    mounted: function () {
        this.getSessionInfo();
        this.getLessons();
    },
    methods: {
        getLessons: function () {
            var self=this;
            $.get(SERVERURL+'lessons', function (data) {
                //se ok
                self.lessonsMatrix = data;
                console.log("Lessons Matrix -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Impossibile reperire le informazioni delle lezioni!");

            });
        },
        getSessionInfo: function () {
            var self=this;
            $.get(SERVERURL+'userlog', function (data) {
                //se ok
                self.user = data;
                if(self.user == null) {
                    window.location = HOMEURL;
                }
                console.log("GetSessionInfo -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Impossibile reperire informazioni utente!");
                window.location = HOMEURL;
            });
        },
        lessonClass: function (state) {
            switch (state) {
                case 1:
                    return "text-dark bg-warning";
                case 2:
                    return "text-white bg-success";
                case 3:
                    return "text-white bg-secondary";
            }
            return "text-white bg-info"
        },
        lessonIcon: function (state) {
            switch (state) {
                case 1:
                    return "fas fa-clock";
                case 2:
                    return "fas fa-check-circle";
                case 3:
                    return "fas fa-times-circle";
                default:
                    return "";
            }
        },
        showModalLesson: function(i,j){
            this.modalLesson.selectedLesson=this.lessonsMatrix[i][j];
            if(this.modalLesson.selectedLesson.state.code==1) {
                this.modalLesson.errorMessage = null;
                this.modalLesson.wantConfirm = false;
                this.modalLesson.wantCancel = false;
                $('#modalState').modal();
            }
        },
        showModalNewReservation: function () {
            var self = this;
            this.modalNewReservation.courses = [];
            this.modalNewReservation.errorMessage = null;
            $.get(HOMEURL+"public/courses", function (data) {
                //se ok
                self.modalNewReservation.courses = data;
                $('#modalNew').modal('show');
                console.log("GetCourses -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                self.modalNewReservation.errorMessage="Si è verificato un errore";
                $('#modalNew').modal('show');
            });
        },
        onSelectCourse: function (e) {
            var code = e.target.value;
            console.log("Selected course code -> "+code);
            alert ("Selected course code -> "+code);
        },
        setModalLessonState: function (n){
            if (n==1){
                this.modalLesson.wantConfirm=true;
                this.modalLesson.wantCancel=false;
            }
            else{
                this.modalLesson.wantConfirm=false;
                this.modalLesson.wantCancel=true;
            }
        },
        saveLessonState: function (){
            var self=this;
            var statecode;
            this.modalLesson.errorMessage=null;
            if(!this.modalLesson.wantConfirm && !this.modalLesson.wantCancel){
                this.modalLesson.errorMessage="Seleziona una azione o annulla";
                return;
            }
            if (this.modalLesson.wantConfirm){
                statecode=2;
            }else {
                statecode=3;
            }



            $.post(SERVERURL+'lessons', {idLesson: this.modalLesson.selectedLesson.id, action:"modificastato", stateLesson: statecode}, function (data) {
                console.log("CheckChangeState -> " + JSON.stringify(data));
                //se ko
                if(!data.result) {
                    self.modalLesson.errorMessage=data.errorOccurred;
                }
                else{
                    $('#modalState').modal('hide');
                    self.getLessons();
                }

            }).fail(function (xhr) {
                console.log("Save lesson state error code "+xhr.status);
                self.modalLesson.errorMessage="Si è verificato un errore";

            });
        },
        showCatalog : function () {
            this.modalCatalog.errorMessage = null;
            this.modalCatalog.catalog = [];
            var self=this;
            $.get(SERVERURL+'catalog', function (data) {
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






