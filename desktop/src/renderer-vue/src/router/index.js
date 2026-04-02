import { createRouter, createWebHashHistory } from "vue-router";
import { useSessionStore } from "@/stores/session";
import ShellLayout from "@/layouts/ShellLayout.vue";
import LoginView from "@/views/LoginView.vue";
import HomeView from "@/views/HomeView.vue";
import TeacherWorkbenchView from "@/views/teacher/TeacherWorkbenchView.vue";
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

  const requiresAuth = ["home", "teacher-workbench", "student-workbench"].includes(String(to.name || ""));
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

  if (to.name === "teacher-workbench" && !sessionStore.isTeacher) {
    return "/student";
  }

  if (to.name === "student-workbench" && !sessionStore.isStudent) {
    return "/teacher";
  }

  return true;
});

export default router;
