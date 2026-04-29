const state = {
  projects: [],
  filter: "ALL"
};

const projectList = document.querySelector("#projectList");
const template = document.querySelector("#projectTemplate");
const form = document.querySelector("#projectForm");
const filterButtons = document.querySelectorAll("[data-filter]");

const labels = {
  PLANNED: "Planned",
  IN_PROGRESS: "In Progress",
  BLOCKED: "Blocked",
  DONE: "Done",
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High"
};

function formatDate(value) {
  const date = new Date(`${value}T12:00:00`);
  return new Intl.DateTimeFormat("en", { month: "short", day: "numeric", year: "numeric" }).format(date);
}

async function fetchJson(url, options) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed with ${response.status}`);
  }
  return response.json();
}

async function loadProjects() {
  const [projects, metrics] = await Promise.all([
    fetchJson("/api/projects"),
    fetchJson("/api/metrics")
  ]);
  state.projects = projects;
  renderMetrics(metrics);
  renderProjects();
}

function renderMetrics(metrics) {
  document.querySelector("#totalMetric").textContent = metrics.total;
  document.querySelector("#activeMetric").textContent = metrics.active;
  document.querySelector("#doneMetric").textContent = metrics.done;
  document.querySelector("#highMetric").textContent = metrics.highPriority;
}

function renderProjects() {
  const projects = state.filter === "ALL"
    ? state.projects
    : state.projects.filter((project) => project.status === state.filter);

  projectList.replaceChildren();

  if (projects.length === 0) {
    const empty = document.createElement("div");
    empty.className = "empty-state";
    empty.textContent = "No projects match this view.";
    projectList.append(empty);
    return;
  }

  projects.forEach((project) => {
    const node = template.content.firstElementChild.cloneNode(true);
    node.querySelector("h3").textContent = project.name;
    node.querySelector(".project-owner").textContent = project.owner;
    node.querySelector(".project-description").textContent = project.description;
    node.querySelector(".due-date").textContent = `Due ${formatDate(project.dueDate)}`;

    const priority = node.querySelector(".priority-pill");
    priority.textContent = labels[project.priority];
    priority.classList.add(`priority-${project.priority}`);

    const status = node.querySelector(".status-pill");
    status.textContent = labels[project.status];
    status.classList.add(`status-${project.status}`);

    node.querySelectorAll("[data-status]").forEach((button) => {
      button.classList.toggle("active", button.dataset.status === project.status);
      button.addEventListener("click", () => updateStatus(project.id, button.dataset.status));
    });

    projectList.append(node);
  });
}

async function createProject(event) {
  event.preventDefault();
  const data = Object.fromEntries(new FormData(form).entries());
  await fetchJson("/api/projects", {
    method: "POST",
    body: JSON.stringify(data)
  });
  form.reset();
  form.elements.status.value = "IN_PROGRESS";
  form.elements.priority.value = "MEDIUM";
  await loadProjects();
}

async function updateStatus(id, status) {
  await fetchJson(`/api/projects/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status })
  });
  await loadProjects();
}

filterButtons.forEach((button) => {
  button.addEventListener("click", () => {
    state.filter = button.dataset.filter;
    filterButtons.forEach((item) => item.classList.toggle("active", item === button));
    renderProjects();
  });
});

form.addEventListener("submit", createProject);
document.querySelector("#refreshButton").addEventListener("click", loadProjects);

loadProjects().catch((error) => {
  projectList.innerHTML = `<div class="empty-state">${error.message}</div>`;
});
