import { validateForm, popupMessage, sleep } from "./validate/validate-form.js";
import("jquery").then(jQuery => {
  window.$ = jQuery.default;
});
import {
  Command, HandicapSetup, RepeatHandicapSetup,
  HandicapData, Course, ListCouresResponse
} from "./handicap/protos/handicap_pb.js";
import { HandicapServiceClient } from "./handicap/protos/handicap_grpc_web_pb.js";
import "../css/app.css";
import "../css/styles.css";
import "bootstrap";
import "../css/dtsel/dtsel.css";
import dtselFunction from "./dtsel/dtsel.js";
import "bootstrap-table/dist/bootstrap-table.css";
import "bootstrap-table";
import protobuf from "protobufjs";
// per https://www.html-code-generator.com/html/drop-down/state-name
import countryState from "./country-states/js/country-states.js";
import "./dodex";
import axios from "axios";
window.axios = axios;
import "./weather.js";

dtselFunction();
countryState("US");

/* Use protobuf to generate/validate message */
const useProtobuf = false;
/*
    For tunneling with `localtunnel` the grpc hostname must be app --subdomain value + "2"
    as described in the README.
*/
let port = isNaN(window.location.hostname.split(".").join("")) ? ":8071" : ":30071"; // for minikube
port = window.location.hostname === "127.0.0.1" ? ":8071" : port;
const grpcHost = window.location.hostname.replace(".loca.lt","2.loca.lt");
const host = window.location.hostname.endsWith(".loca.lt") ? grpcHost : window.location.hostname;
port = grpcHost.endsWith("2.loca.lt") ? "" : port;

const client = new HandicapServiceClient(window.location.protocol + "//" + host + port, null, null);
const login = new HandicapSetup();
const command = new Command();
const courses = new Course();
const rgb2Hex = s => s.match(/[0-9]+/g).reduce((a, b) => a + (b | 256).toString(16).slice(1), "#");
let coursesData = null;

new dtsel.DTS('input[name="tee_time"]', {
  direction: "TOP",
  dateFormat: "yyyy-mm-dd",
  showTime: true,
  timeFormat: "HH:MM"
});

let formatter = Intl.DateTimeFormat(
  "default", // a locale name; "default" chooses automatically
  {
    year: "numeric",
    month: "numeric",
    day: "numeric",
    hour: "numeric",
    minute: "numeric",
    hour12: true,
    visible: true
  }
);

function selectedTee() {
  let selectedTee;
  const radioColors = document.querySelectorAll("input[name=radio-color]");
  for (const radioTee of radioColors) {
    if (radioTee.checked) {
      selectedTee = radioTee;
      break;
    }
  }
  return selectedTee;
}

function round(num, precision) {
  precision = Math.pow(10, precision)
  return Math.floor(num * precision) / precision
}

const submitLogin = async (event) => {
  const isValid = validateForm(true);
  if (!isValid) {
    return;
  }
  event.preventDefault();

  let message = JSON.stringify(golferData());
  let newLogin = typeof window.pin !== "undefined" && window.pin !== document.getElementById("pin").value;
  window.cmd = typeof window.cmd === "undefined" || newLogin ? 3 : 8;

  login.setJson(message);
  login.setCmd(window.cmd);
  login.setMessage("Golfer Data");

  await client.getGolfer(login, {}, (err, response) => {
    if (err) {
      console.error(`Unexpected error for getGolfer: code = ${err.code}` +
        `, message = "${err.message}"`);
    } else {
      window.pin = null;
      const responseJson = JSON.parse(response.getJson());

      if (responseJson.status < 0) {
        popupMessage(responseJson.message, "alert-danger");
      } else {
        document.getElementById("pin").value = responseJson.pin;
        document.getElementById("country").value = responseJson.country;
        document.getElementById("state").value = responseJson.state;
        document.getElementById("fname").value = String.fromHtmlEntities(responseJson.firstName);
        document.getElementById("lname").value = String.fromHtmlEntities(responseJson.lastName);
        let handicap = Number(responseJson.handicap);
        if(handicap < 0) {
          handicap = "(+)" + (handicap * -1);
        }
        document.getElementById("handicap").value = round(handicap, 1)
        document.getElementById("overlap").checked = responseJson.overlap;
        document.getElementById("public").checked = responseJson.public;
        document.getElementById("ggc").className = "gg-check-o";
        document.getElementById("lfor").className = "float-end";
        window.pin = responseJson.pin;

        if((responseJson.adminstatus && responseJson.adminstatus === 10) && responseJson.pin !== responseJson.admin) {
          const addCourse = document.getElementById("add-course");
          addCourse.ariaDisabled = true;
          addCourse.style.opacity = ".5";
          addCourse.onclick = function () {
              popupMessage("Add Course/Tee disabled: See Adminstrator", "alert-warning")
          }
        }
      }
      if (coursesData === null || typeof window.pin !== "undefined") {
        listCourses()
//        getCourses()
      }
    }
  });
}

async function getCourses(dataOnly = false) {
  command.setCmd(1);
  command.setKey(document.getElementById("state").value);

  await client.getCourses(command, {}, (err, response) => {
    if (err) {
      console.error(`Unexpected error for List Courses: code = ${err.code}` +
      `, message = "${err.message}"`);
    } else {
      coursesData = JSON.parse(response.getJson());
      const list = document.querySelectorAll("#courses > option");

      if (!dataOnly) {
        if (list.length > 1) {
          for (const opt of list) {
            if (opt.value !== "") {
              opt.remove();
            }
          }
        }

        for (let i = 0; i < coursesData.courses.length; i++) {
          const att = document.createAttribute("key");
          att.value = coursesData.courses[i].id;

          var option = document.createElement("option");
          option.setAttributeNode(att);
          option.value = String.fromHtmlEntities(coursesData.courses[i].name);
          option.innerHTML = String.fromHtmlEntities(coursesData.courses[i].name);
          list[0].parentNode.insertBefore(option, list[0].lastSibling);
        }
      }
    }
  })
}

async function listCourses(dataOnly = false) {
  command.setCmd(1);
  command.setKey(document.getElementById("state").value);

  await client.listCourses(command, {}, (err, response) => {
    if (err) {
      console.error(`Unexpected error for List Courses: code = ${err.code}` +
        `, message = "${err.message}"`);
    } else {
      coursesData = response.getCoursesList();
      const list = document.querySelectorAll("#courses > option");

      if (!dataOnly) {
        if (list.length > 1) {
          for (const opt of list) {
            if (opt.value !== "") {
              opt.remove();
            }
          }
        }

        for(const course of coursesData) {
          const att = document.createAttribute("key");
          att.value = course.getId()
          var option = document.createElement("option");
          option.setAttributeNode(att);
          option.value = String.fromHtmlEntities(course.getName());
          option.innerHTML = String.fromHtmlEntities(course.getName());
          list[0].parentNode.insertBefore(option, list[0].lastSibling);
        }
      }
    }
  })
}

const submitAddCourse = async (event) => {
  const isValid = validateForm(true, true);

  if (!isValid) {
    return;
  }
  event.preventDefault();

  const message = ratingData();
  const ratingJson = JSON.stringify(message);

  command.setCmd(2);
  command.setKey(message.seq);
  command.setJson(ratingJson);

  await client.addRating(command, {}, (err, response) => {
    if (err) {
      console.log(`Unexpected error for add Rating: code = ${err.code}` +
        `, message = "${err.message}"`);
      popupMessage("Course Tees Add Failed", "alert-danger");
    } else {
      let isDataOnly = true;
      if(typeof ratingJson.seq === "undefined") {
        isDataOnly = false;
      }
      popupMessage("Course Tees Added", "alert-success");
      sleep(500).then(() => {
        listCourses(isDataOnly)
//        getCourses(isDataOnly)
      });
    }
  });
}

const submitAddScore = async (event) => {
  const isValid = validateForm(true, true, true);

  if (!isValid) {
    return;
  }
  event.preventDefault();
  const message = scoreData();
  const scoreJson = JSON.stringify(message);

  command.setCmd(4);
  command.setKey(message.course.seq);
  command.setJson(scoreJson);

  await client.addScore(command, {}, (err, response) => {
    if (err) {
      console.log(`Unexpected error for addScore: code = ${err.code}` +
        `, message = "${err.message}"`);
        popupMessage("Score Add Failed", "alert-danger");
      } else {
      const responseJson = JSON.parse(response.getJson());
      let handicap = Number(responseJson.handicap);
      if(handicap < 0) {
        handicap = "(+)" + (handicap * -1);
      }
      document.getElementById("handicap").value = handicap; // responseJson.golfer.handicap;

      const cHandicap = toNumberOnly(courseHandicap());
      const net = responseJson.grossScore - (cHandicap !== null ? Number(cHandicap) : Number(responseJson.golfer.handicap));
      document.getElementById("net-score").value = net;
      popupMessage("Score Added", "alert-success");
    }
  });
}

const submitGolferScores = async (event) => {
    const message = {};
  if(event && event.target.value.length !== 0) {
    command.setCmd(10);
    command.setKey(event.target.value.toHtmlEntities());
  } else {
      message.pin = document.getElementById("pin").value
      message.overlap = document.getElementById("overlap").checked
      message.public = document.getElementById("public").checked
      command.setCmd(6);
      command.setKey(message.pin);
  }
  const scoreJson = JSON.stringify(message);
  command.setJson(scoreJson);

  await client.golferScores(command, {}, (err, response) => {
    if (err) {
      console.log(`Unexpected error for golfer scores: code = ${err.code}` +
        `, message = "${err.message}"`);
    } else {
      const responseJson = JSON.parse(response.getJson());
      const table = window.$("#table")

      $(function () {
        const len = table.bootstrapTable("getData")? table.bootstrapTable("getData").length: 0
        if (len > 1) {
          table.bootstrapTable("load", responseJson)
        } else {
          table.bootstrapTable("destroy").bootstrapTable({ data: responseJson })
        }
      })
    }
  });
}

const submitGetGolfers = async (event) => {
  command.setCmd(9);
  await client.getGolfers(command, {}, (err, response) => {
    if (err) {
      console.log(`Unexpected error for get golfers: code = ${err.code}` +
      `, message = "${err.message}"`);
    } else {
      const list = document.querySelectorAll("#golfers > option")
      if (list.length > 1) {
        for (const opt of list) {
          if (opt.value !== "") {
            opt.remove();
          }
        }
      }
      const responseJson = JSON.parse(response.getJson());
      for (let i = 0; i < responseJson.golfer.length; i++) {
        var option = document.createElement("option");
        const name = String.fromHtmlEntities(responseJson.golfer[i]);
        option.value = name;
        option.innerHTML = name;
        list[0].parentNode.insertBefore(option, list[0].lastSibling);
      }
    }
  });
}

const submitListGolfers = async (event) => {
  command.setCmd(9);
  
  await client.listGolfers(command, {}, (err, response) => {
    if (err) {
      console.log(`Unexpected error for get golfers: code = ${err.code}` +
        `, message = "${err.message}"`);
    } else {
      const list = document.querySelectorAll("#golfers > option")
      if (list.length > 1) {
        for (const opt of list) {
          if (opt.value !== "") {
            opt.remove();
          }
        }
      }
      for (let i = 0; i < response.array[0].length; i++) {
        var option = document.createElement("option");
        const name = String.fromHtmlEntities(response.array[0][i][0]);
        option.value = name;
        option.innerHTML = name;
        list[0].parentNode.insertBefore(option, list[0].lastSibling);
      }
    }
  });
}

const submitRemoveScore = async (event) => {
  const isValid = validateForm(true, false, false);
  if (!isValid || !confirm("𝐀𝐫𝐞 𝐲𝐨𝐮 𝐬𝐮𝐫𝐞?") == true) {
    return;
  }
  event.preventDefault();
  const message = {};
  message.pin = document.getElementById("pin").value
  message.overlap = document.getElementById("overlap").checked
  const scoreJson = JSON.stringify(message);

  command.setCmd(7);
  command.setKey(message.pin);
  command.setJson(scoreJson);

  await client.removeScore(command, {}, (err, response) => {
    if (err) {
      console.log(`Unexpected error for removeScore: code = ${err.code}` +
        `, message = "${err.message}"`);
        popupMessage("Removal Failed", "alert-danger")
  } else {
      const responseJson = JSON.parse(response.getJson());
      if(responseJson.status == "-1") {
        popupMessage("Removal Failed", "alert-danger")
        return
      }
      let handicap = Number(responseJson.handicap);
      if(handicap < 0) {
        handicap = "(+)" + (handicap * -1);
      }
      document.getElementById("handicap").value = handicap; // responseJson.handicap;
    }
    popupMessage("Last Score Removed", "alert-success")
  });
}

function findCourse(name) {
  let returnCourse = null;
  if (coursesData !== null) {
    for(const course of coursesData) {
      returnCourse = course
      if (course.getName() === name) {
        break;
      }
      returnCourse = null;
    }
  }
  return returnCourse;
}

function findGetCourse(name) {
  let course = null;
  if (coursesData !== null) {
    for (let i = 0; i < coursesData.courses.length; i++) {
      course = coursesData.courses[i];
      if (course.name === name) {
        break;
      }
      course = null;
    }
  }
  return course;
}

function setGetTees(event) {
  event.preventDefault();

  const courseName = document.querySelector("#course").value;
  const course = findGetCourse(courseName);
  const checked = document.querySelector('input[name="radio-color"]:checked').value;
  const rating = document.getElementById("rating");
  const slope = document.getElementById("slope");
  const par = document.getElementById("par");
  rating.value = slope.value = par.value = "";

  if (course === null) {
    return;
  }

  const selectTee = selectedTee();
  const labels = selectTee.labels;

  for (let i = 0; i < course.ratings.length; i++) {
    const ratings = course.ratings[i]

    if (typeof ratings.tee === "undefined") { // protobuf treats 0 as null(undefined)
      ratings.tee = 0;
    }
    if (checked == ratings.tee) {
      if (typeof ratings.rating !== "undefined" && ratings.rating) {
        rating.value = ratings.rating;
        slope.value = ratings.slope;
        par.value = ratings.par;
        if (ratings.color) {
          document.getElementById("tees").value = ratings.color;
        }
      } else {
        if (labels[0].style.backgroundColor.startsWith("#")) {
          document.getElementById("tees").value = labels[0].style.backgroundColor;
        } else {
          document.getElementById("tees").value = rgb2Hex(labels[0].style.backgroundColor);
        }
      }
    }
    // Set with possible custom color
    if (ratings.rating) {
      document.querySelector(`label[for='radio-color${ratings.tee + 1}']`).style.backgroundColor = ratings.color;
    }
  }

  courseHandicap()
}

function setTees(event) {
  event.preventDefault();

  const courseName = document.querySelector("#course").value;
  const course = findCourse(courseName);
  const checked = document.querySelector('input[name="radio-color"]:checked').value;
  const rating = document.getElementById("rating");
  const slope = document.getElementById("slope");
  const par = document.getElementById("par");
  rating.value = slope.value = par.value = "";
  if (course === null) {
    return;
  }

  const selectTee = selectedTee();
  const labels = selectTee.labels;

  for (const ratingTee of course.getRatingsList()) {
    if (typeof ratingTee.getTee() === "undefined") { // protobuf treats 0 as null(undefined)
      ratingTee.tee = 0;
    }
    if (checked == ratingTee.getTee()) {
      if (typeof ratingTee.getRating() !== "undefined" && ratingTee.getRating()) {
        rating.value = ratingTee.getRating()
        slope.value = ratingTee.getSlope()
        par.value = ratingTee.getPar()
        if (ratingTee.getColor()) {
          document.getElementById("tees").value = ratingTee.getColor();
        }
      } else {
        if (labels[0].style.backgroundColor.startsWith("#")) {
          document.getElementById("tees").value = labels[0].style.backgroundColor;
        } else {
          document.getElementById("tees").value = rgb2Hex(labels[0].style.backgroundColor);
        }
      }
    }
    // Set with possible custom color
    if (ratingTee.getRating()) {
      document.querySelector(`label[for='radio-color${ratingTee.getTee() + 1}']`).style.backgroundColor = ratingTee.getColor();
    }
  }

  courseHandicap()
}

var courseHandicap = () => {
  const handicap = toNumberOnly(document.querySelector("#handicap").value);
  const cHandicap = document.querySelector("#course-handicap");
  const rating = document.getElementById("rating");
  const slope = document.getElementById("slope");
  const par = document.getElementById("par");
  if (rating &&
    slope &&
    par &&
    handicap != 0 
  ) {
    let courseHandicap = (Number(handicap) * Number(slope.value) / 113 + (Number(rating.value) - Number(par.value))).toFixed(1);
    if(courseHandicap < 0) {
      courseHandicap = courseHandicap * -1;
      courseHandicap = "(+)" + courseHandicap;
    }
    cHandicap.value = courseHandicap;
  } else {
    cHandicap.value = null;
  }
  return cHandicap.value
}

var setColor = (event) => {
  let element = selectedTee();
  let labels;
  let id = element.id;

  if (element.labels) {
    labels = element.labels;
  } else if (id) {
    labels = document.querySelectorAll(`label[for='${id}']`);
    while (element = element.parentNode) {
      if (element.tagName.toLowerCase() == "label") {
        labels.push(element);
      }
    }
  }

  for (const label of labels) {
    label.style.backgroundColor = document.querySelector("#tees").value;
  };
}

function golferData() {
  var golferObject = {};

  golferObject.pin = document.getElementById("pin").value;
  golferObject.firstName = document.getElementById("fname").value.toHtmlEntities();
  golferObject.lastName = document.getElementById("lname").value.toHtmlEntities();
  golferObject.country = document.getElementById("country").value || "US";
  golferObject.state = document.getElementById("state").value || "NV";
  golferObject.overlap = document.getElementById("overlap").checked
  golferObject.public = document.getElementById("public").checked
  golferObject.lastLogin = Date.now()
  golferObject.status = 0;
  golferObject.course = document.getElementById("course").value.toHtmlEntities();

  golferObject.tee = selectedTee().value;
  const teetimeValue = document.getElementById("teetime").value;
  golferObject.teeDate = teetimeValue ? Date.parse(teetimeValue.substring(0, 8)) : new Date().getTime();

  let message;
  if (useProtobuf) {
    const golferDescriptor = require("./handicap/json/golfer.json");
    const root = protobuf.Root.fromJSON(golferDescriptor);
    const golferMessage = root.lookupType("handicap.GolferMessage");
    message = golferMessage.create(golferObject);
    const errMessage = golferMessage.verify(message);

    if (errMessage) {
      console.warn("Json verify problem:", errMessage)
      // throw errMessage
    }
  }

  return useProtobuf ? message : golferObject;
}

function ratingData() {
  const ratingObject = {};
  ratingObject.pin = document.getElementById("pin").value;
  ratingObject.courseName = document.querySelector("#course").value.toHtmlEntities();
  ratingObject.tee = Number(selectedTee().value);
  ratingObject.country = document.getElementById("country").value;
  ratingObject.state = document.getElementById("state").value;
  ratingObject.color = document.querySelector(`label[for='${selectedTee().id}']`).style.backgroundColor;
  ratingObject.rating = document.getElementById("rating").value;
  ratingObject.slope = Number(document.getElementById("slope").value);
  ratingObject.par = Number(document.getElementById("par").value);
  ratingObject.status = 0;
  const options = document.querySelectorAll("#courses > option");
  for (let idx = 0; idx < options.length; idx++) {
    if (options[idx].value === String.fromHtmlEntities(ratingObject.courseName)) {
      ratingObject.seq = Number(options[idx].getAttribute("key"));
      break;
    }
  }
  let message;
  if (useProtobuf) {
    const ratingDescriptor = require("./handicap/json/rating.json");
    const root = protobuf.Root.fromJSON(ratingDescriptor);
    const ratingMessage = root.lookupType("handicap.RatingMessage");
    message = ratingMessage.create(ratingObject);
    const errMessage = ratingMessage.verify(message);

    if (errMessage) {
      console.warn("Json verify problem:", errMessage)
      // throw errMessage
    }
  }

  return useProtobuf ? message : ratingObject;
}

function toNumberOnly(handicap) {
  if(handicap !== null && isNaN(handicap)) {
    return handicap.substring(3) * -1;
  }
  return handicap;
}

function scoreData() {
  const scoreObject = {};
  const golferObject = {};
  const courseObject = {};
  golferObject.pin = document.getElementById("pin").value;
  golferObject.overlap = document.getElementById("overlap").checked
  golferObject.public = document.getElementById("public").checked
  const handicap = toNumberOnly(document.getElementById("handicap").value);
  golferObject.handicap = handicap; // document.getElementById("handicap").value;
  courseObject.courseName = document.querySelector("#course").value.toHtmlEntities();
  courseObject.teeId = Number(selectedTee().value);
  scoreObject.grossScore = Number(document.getElementById("total-score").value);
  scoreObject.netScore = Number(document.getElementById("net-score").value);
  scoreObject.adjustedScore = Number(document.getElementById("adjusted-score").value);
  scoreObject.teeTime = getTeeTime();
  scoreObject.golfer = golferObject;
  scoreObject.course = courseObject;
  scoreObject.status = 0;

  const options = document.querySelectorAll("#courses > option");
  for (const idx in options) {
    if (options[idx].value === String.fromHtmlEntities(courseObject.courseName)) {
      courseObject.course_key = Number(options[idx].getAttribute("key"));
      break;
    }
  }
  let message;
  if (useProtobuf) {
    const scoreDescriptor = require("./handicap/json/score.json");
    const root = protobuf.Root.fromJSON(scoreDescriptor);
    const scoreMessage = root.lookupType("handicap.ScoreMessage");
    message = scoreMessage.create(scoreObject);
    const errMessage = scoreMessage.verify(message);

    if (errMessage) {
      console.warn("Json verify problem:", errMessage)
      // throw errMessage
    }
  }

  return useProtobuf ? message : scoreObject;
}

const getTeeTime = () => {
  const teetimeArray = document.getElementById("teetime").value.split(",");

  if (teetimeArray.length < 2) {
    return new Date();
  }

  return teetimeArray[0] + "T" + teetimeArray[1].trim();
}

function defaultData() {
  var rows = []

  for (var i = 0; i < 10; i++) {
    rows.push({
      COURSE_NAME: ""
    })
  }
  return rows
}

function resetForm() {
  delete window.pin;
  delete window.cmd;
}

(() => {
  var images = ['golf01.jpg', 'golf02.jpg', 'golf03.jpg', 'golf04.jpg', 'golf05.jpg',
                'golf06.jpg', 'golf07.jpg', 'golf08.jpg', 'golf09.jpg', 'golf10.jpg', 'golf11.jpg'];
  document.querySelector("body").style.backgroundImage = "url(./handicap/" + images[Math.floor(Math.random() * images.length)] + ")";
  document.querySelector("form").reset();
  const login = document.getElementById("login");
  login.addEventListener("click", submitLogin);
  const course = document.querySelector("#add-course");
  course.addEventListener("click", submitAddCourse);
  const score = document.querySelector("#add-score");
  score.addEventListener("click", submitAddScore);
  const courses = document.getElementById("course");
  courses.addEventListener("change", setTees);
//  courses.addEventListener("change", setGetTees);
  document.querySelector(".form-control-color").addEventListener("change", setColor);
  const radioColor = document.querySelectorAll('input[name="radio-color"]');
  for (const idx in radioColor) {
    if (typeof radioColor[idx].addEventListener === "function") {
      radioColor[idx].addEventListener("change", setTees);
//      radioColor[idx].addEventListener("change", setGetTees);
    }
  }
  const state = document.getElementById("state");
  state.addEventListener("change", listCourses);
//  state.addEventListener("change", getCourses);

  const removeLast = document.getElementById("remove-last");
  removeLast.addEventListener("click", submitRemoveScore);
  const tabEl = document.querySelector("#scores-tab")
  tabEl.addEventListener("show.bs.tab", event => {
    const isValid = validateForm(true, false, false);
    if (!isValid) {
      return;
    }
    submitGolferScores()
//    submitListGolfers()
    submitGetGolfers()
  })

  const golfers = document.querySelector("#golfers")
  golfers.addEventListener("change", event => {
    submitGolferScores(event)
  })
  const cancel = document.querySelector("#cancel");
  cancel.addEventListener("click", resetForm);

  // For webpack bundler
  setTimeout(function () {
    const table = window.$("#table")
    $(function () {
      table.bootstrapTable({ data: defaultData() })
    })
  }, 500);
})();