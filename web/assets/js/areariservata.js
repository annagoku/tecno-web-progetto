//URL della pagina corrente
var SERVERURL = window.location.href;
var HOMEURL = SERVERURL.substr(0, SERVERURL.indexOf("private"));

window.userSession = null;

var areaRiservataApp= new Vue ({
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
            errorMessage: null,
            reservationSelected: null
        },
        modalCheckReservation: {
            state: null,
            feedbackMessage: null
        }

    },
    mounted: function () {
        this.getSessionInfo();
        this.getLessons();
    },
    methods: {
//recupera da back end le lezioni prenotate dall'utente
        getLessons: function () {
            var self = this;
            $.get(SERVERURL + 'lessons', function (data) {
                //se ok
                self.lessonsMatrix = data;
                console.log("Lessons Matrix -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Impossibile reperire le informazioni delle lezioni!");

            });
        },

//recupera le informazioni sull'utente che ha fatto login
        getSessionInfo: function () {
            var self = this;
            $.get(SERVERURL + 'userlog', function (data) {
                //se ok
                self.user = data;
                if (self.user == null) {
                    window.location = HOMEURL;
                }
                console.log("GetSessionInfo -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                alert("Impossibile reperire informazioni utente!");
                window.location = HOMEURL;
            });
        },

//rappresenta in modo diverso le lezioni prenotate in base al loro stato : prenotato-confermato-annullato
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

//rappresenta in modo diverso le lezioni prenotate in base al loro stato associando anche la relativa icona: prenotato-confermato-annullato
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

//apertura di una modale per cambiare lo stato di una prenotazione
        showModalLesson: function (k) {
            this.modalLesson.selectedLesson = k;
            if (this.modalLesson.selectedLesson.state.code == 1) {
                this.modalLesson.errorMessage = null;
                this.modalLesson.wantConfirm = false;
                this.modalLesson.wantCancel = false;
                $('#modalState').modal();
            }
        },

//Variazione dello stato di una prenotazione effettuata
        setModalLessonState: function (n) {
            if (n == 1) {
                this.modalLesson.wantConfirm = true;
                this.modalLesson.wantCancel = false;
            } else {
                this.modalLesson.wantConfirm = false;
                this.modalLesson.wantCancel = true;
            }
        },

//Salvataggio del nuovo stato di una prenotazione
        saveLessonState: function () {
            var self = this;
            var statecode;
            this.modalLesson.errorMessage = null;
            if (!this.modalLesson.wantConfirm && !this.modalLesson.wantCancel) {
                this.modalLesson.errorMessage = "Seleziona una azione o annulla";
                return;
            }
            if (this.modalLesson.wantConfirm) {
                statecode = 2;
            } else {
                statecode = 3;
            }
            $.post(SERVERURL + 'lessons', {
                idLesson: this.modalLesson.selectedLesson.id,
                action: "modificastato",
                stateLesson: statecode
            }, function (data) {
                console.log("CheckChangeState -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    self.modalLesson.errorMessage = data.errorOccurred;
                } else {
                    $('#modalState').modal('hide');
                    self.getLessons();
                }

            }).fail(function (xhr) {
                console.log("Save lesson state error code " + xhr.status);
                self.modalLesson.errorMessage = "Si è verificato un errore";

            });
        },

//Visualizzazione dell'intero catalogo di lezioni disponibili
        showCatalog: function () {
            this.modalCatalog.errorMessage = null;
            this.modalCatalog.catalog = [];
            var self = this;
            $.get(SERVERURL + 'catalog', function (data) {
                //se ok
                self.modalCatalog.catalog = data;
                console.log("Lessons catalog -> " + JSON.stringify(data));
                $("#modalCatalog").modal('show');
            }).fail(function () {
                //se errore
                self.modalCatalog.errorMessage = "Impossibile reperire i dati, riprovare più tardi";
                $("#modalCatalog").modal('show');
            });
        },
//apertura di una modale con selezione della materia di interesse e visualizzazione delle disponibilità
        showModalNewReservation: function () {
            var self = this;
            this.modalNewReservation.courses = [];
            this.modalNewReservation.matrix = null;
            this.modalNewReservation.errorMessage = null;
            $.get(HOMEURL + "public/courses", function (data) {
                //se ok
                self.modalNewReservation.courses = data;
                $('#modalNew').modal('show');
                console.log("GetCourses -> " + JSON.stringify(data));
            }).fail(function () {
                //se errore
                self.modalNewReservation.errorMessage = "Si è verificato un errore";
                $('#modalNew').modal('show');
            });
        },

//selezionata la materia permette di caricarne su timetable le disponibilità
        onSelectCourse: function (e) {
            var code = e.target.value;
            var self = this;
            $.get(SERVERURL + 'courseavailability?courseCode=' + code, function (data) {
                console.log("Select availability course -> " + JSON.stringify(data));

                self.modalNewReservation.matrix = data;

            }).fail(function (xhr) {
                console.log("Retrieve course availability " + xhr.status);
                self.modalNewReservation.errorMessage = "Si è verificato un errore";

            });
            console.log("Selected course code -> " + code);

        },
//selezione di una nuova prenotazione tramite click su timetable
        clickReservation: function (catalogItem) {
            //console.log("Item selected " + JSON.stringify(catalogItem))
            if (this.modalNewReservation.reservationSelected != null) {
                this.modalNewReservation.reservationSelected.selected = false;
            }
            catalogItem.selected = true;
            this.modalNewReservation.reservationSelected = catalogItem;

        },

//controlla e gestisce eventuali sovrapposizioni tra nuova prenotazione selezionata e prenotazioni esistenti
        checkFeasibilityNewReservation : function(){
            $('#modalNew').modal('hide');
            console.log("checkFeasibility");
            var j;
            var i;

            this.modalCheckReservation.feedbackMessage=null;
            if(this.modalNewReservation.reservationSelected) {
                i = this.modalNewReservation.reservationSelected.slot.id - 1;
                j = this.modalNewReservation.reservationSelected.day.daycode - 1;
                console.log("checkFeasibility i: "+i+", j: "+j);
                if (this.lessonsMatrix[i][j].length > 0) {
                    console.log(this.lessonsMatrix[i][j]);
                    this.modalCheckReservation.state = 3;
                    for(var k=0; k<this.lessonsMatrix[i][j].length; k++) {
                        if(this.lessonsMatrix[i][j][k].state.code !== '3') {
                            this.modalCheckReservation.state = this.lessonsMatrix[i][j][k].state.code;
                        }
                    }

                    switch (this.modalCheckReservation.state) {
                        case 1:
                            $('#modalCheckFeasibility').modal('show');
                            return this.modalCheckReservation.feedbackMessage = "Hai già una prenotazione nello slot scelto. Vuoi annullare e sovrascrivere?";
                        case 2 :
                            $('#modalCheckFeasibility').modal('show');
                            return this.modalCheckReservation.feedbackMessage = "Hai già una prenotazione effettuata nello slot scelto. Impossibile sostituire una prenotazione effettuata";
                        case 3:
                            console.log("checkFeasibility -> posso effettuare la prenotazione");
                            return this.saveNewReservation();

                    }
                } else {
                    console.log("checkFeasibility -> posso effettuare la prenotazione");
                    return this.saveNewReservation();
                }
            }else{
                this.modalNewReservation.errorMessage="Seleziona un lezione da prenotare o annulla";
            }
        },

//Salvataggio nuova prenotazione su db
        saveNewReservation: function () {
            console.log("saveNewReservation");
            $('#modalCheckFeasibility').modal('hide');
            var self=this;
            this.modalNewReservation.reservationSelected.selected = false;
            $.post(SERVERURL + 'newReservation', {
                infoCatalogItemSelected: JSON.stringify(this.modalNewReservation.reservationSelected),
            }, function (data) {
                console.log("CheckNewReservation -> " + JSON.stringify(data));
                //se ko
                if (!data.result) {
                    alert(data.errorMessage);
                } else {
                    $('#modalNew').modal('hide');
                    self.getLessons();
                }
            }).fail(function (xhr) {
                console.log("Save new lesson error code " + xhr.status);
                alert("Errore nel salvataggio nuova prenotazione  -> status " + xhr.status);

            });
        },

//Logout utente e ritorno alla home
        logoutAction: function () {
            var self = this;
            $.get(SERVERURL + 'logout', function (data) {
                //se ok
                console.log("Logout -> " + JSON.stringify(data));
                if (data.result == true) {
                    document.location.href = HOMEURL;
                } else {
                    alert(data.errorMessage);
                }
            }).fail(function (xhr) {
                alert("Errore sul logout -> status " + xhr.status);
            });
        }
    }
});






