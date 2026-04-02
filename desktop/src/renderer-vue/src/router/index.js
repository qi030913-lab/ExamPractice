import { createRouter, createWebHashHistory } from "vue-router";
import { useSessionStore } from "@/stores/session";
import ShellLayout from "@/layouts/ShellLayout.vue";
import LoginView from "@/views/LoginView.vue";
import HomeView from "@/views/HomeView.vue";
import TeacherWorkbenchView from "@/views/teacher/TeacherWorkbenchView.vue";
import TeacherPapersView from "@/views/teacher/TeacherPapersView.vue";
import TeacherPaperDetailView from "@/views/teacher/TeacherPaperDetailView.vue";
import TeacherStudentsView from "@/views/teacher/TeacherStudentsView.vue";
import TeacherImportView from "@/views/teacher/TeacherImportView.vue";
import StudentWorkbenchView from "@/views/student/StudentWorkbenchView.vue";

const routes = [
  {
    path: "/",
    component: ShellLayout,
    children: [
      {
        path: "",
        redirect: "/login"
      },
      {
        path: "login",
        name: "login",
        component: LoginView
      },
      {
        path: "home",
        name: "home",
        component: HomeView
      },
      {
        path: "teacher",
        name: "teacher-workbench",
        component: TeacherWorkbenchView
      },
      {
        path: "teacher/papers",
        name: "teacher-papers",
        component: TeacherPapersView
      },
      {
        path: "teacher/papers/:paperId",
        name: "teacher-paper-detail",
        component: TeacherPaperDetailView
      },
      {
        path: "teacher/import",
        name: "teacher-import",
        component: TeacherImportView
      },
      {
        path: "teacher/students",
        name: "teacher-students",
        component: TeacherStudentsView
      },
      {
        path: "student",
        name: "student-workbench",
        component: StudentWorkbenchView
      }
    ]
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

router.beforeEach(async (to) => {
  const sessionStore = useSessionStore();

  if (to.name === "login" && sessionStore.isLoggedIn) {
    return sessionStore.isTeacher ? "/teacher" : "/student";
  }

  const requiresAuth = [
    "home",
    "teacher-workbench",
    "teacher-papers",
    "teacher-paper-detail",
    "teacher-import",
    "teacher-students",
    "student-workbench"
  ].includes(String(to.name || ""));

  if (!requiresAuth) {
    return true;
  }

  if (!sessionStore.isLoggedIn) {
    return "/login";
  }

  if (!sessionStore.workbench) {
    try {
      await sessionStore.loadWorkbench();
    } catch (_error) {
      sessionStore.logout();
      return "/login";
    }
  }

  const teacherRoutes = ["teacher-workbench", "teacher-papers", "teacher-paper-detail", "teacher-import", "teacher-students"];
  if (teacherRoutes.includes(String(to.name || "")) && !sessionStore.isTeacher) {
    return "/student";
  }

  if (to.name === "student-workbench" && !sessionStore.isStudent) {
    return "/teacher";
  }

  return true;
});

export default router;
