const { useEffect, useMemo, useState } = React;
const h = React.createElement;

const statusLabels = {
  RECEIVED: "Received",
  PREPARING: "Preparing",
  OUT_FOR_DELIVERY: "Out for delivery",
  DELIVERED: "Delivered"
};

const nextStatuses = ["RECEIVED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED"];

function money(cents) {
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(cents / 100);
}

async function fetchJson(url, options) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({ error: "Request failed" }));
    throw new Error(body.error || `Request failed with ${response.status}`);
  }
  return response.json();
}

function App() {
  const [menu, setMenu] = useState([]);
  const [orders, setOrders] = useState([]);
  const [metrics, setMetrics] = useState({ menuItems: 0, openOrders: 0, completedOrders: 0, revenueCents: 0 });
  const [cart, setCart] = useState({});
  const [category, setCategory] = useState("All");
  const [checkout, setCheckout] = useState({ customerName: "", address: "", notes: "" });
  const [message, setMessage] = useState("");

  async function loadData() {
    const [menuData, orderData, metricData] = await Promise.all([
      fetchJson("/api/menu"),
      fetchJson("/api/orders"),
      fetchJson("/api/metrics")
    ]);
    setMenu(menuData);
    setOrders(orderData);
    setMetrics(metricData);
  }

  useEffect(() => {
    loadData().catch((error) => setMessage(error.message));
  }, []);

  const categories = useMemo(() => ["All", ...new Set(menu.map((item) => item.category))], [menu]);
  const filteredMenu = category === "All" ? menu : menu.filter((item) => item.category === category);
  const cartLines = Object.entries(cart)
    .map(([id, quantity]) => ({ item: menu.find((menuItem) => menuItem.id === Number(id)), quantity }))
    .filter((line) => line.item);
  const cartTotal = cartLines.reduce((sum, line) => sum + line.item.priceCents * line.quantity, 0);

  function addToCart(item) {
    setCart((current) => ({ ...current, [item.id]: (current[item.id] || 0) + 1 }));
  }

  function changeQuantity(itemId, delta) {
    setCart((current) => {
      const nextQuantity = (current[itemId] || 0) + delta;
      const next = { ...current };
      if (nextQuantity <= 0) {
        delete next[itemId];
      } else {
        next[itemId] = nextQuantity;
      }
      return next;
    });
  }

  async function submitOrder(event) {
    event.preventDefault();
    if (cartLines.length === 0) {
      setMessage("Add at least one item before checkout.");
      return;
    }

    const items = cartLines.map((line) => `${line.item.id}:${line.quantity}`).join(",");
    await fetchJson("/api/orders", {
      method: "POST",
      body: JSON.stringify({ ...checkout, items })
    });
    setCart({});
    setCheckout({ customerName: "", address: "", notes: "" });
    setMessage("Order placed. The kitchen has it now.");
    await loadData();
  }

  async function updateOrderStatus(orderId, status) {
    await fetchJson(`/api/orders/${orderId}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status })
    });
    await loadData();
  }

  return h("div", { className: "app" },
    h("header", { className: "hero" },
      h("div", null,
        h("p", { className: "eyebrow" }, "Java + React Food Ordering"),
        h("h1", null, "BiteFlow"),
        h("p", { className: "lede" }, "Browse the menu, build an order, and move tickets through the kitchen from one full-stack app.")
      ),
      h("button", { className: "refresh", onClick: () => loadData() }, "Refresh")
    ),
    h("main", { className: "shell" },
      h("section", { className: "metrics", "aria-label": "Restaurant metrics" },
        h(Metric, { label: "Menu Items", value: metrics.menuItems }),
        h(Metric, { label: "Open Orders", value: metrics.openOrders }),
        h(Metric, { label: "Delivered", value: metrics.completedOrders }),
        h(Metric, { label: "Revenue", value: money(metrics.revenueCents) })
      ),
      message && h("div", { className: "notice" }, message),
      h("section", { className: "grid" },
        h("div", { className: "menu-area" },
          h("div", { className: "section-head" },
            h("h2", null, "Menu"),
            h("div", { className: "tabs", role: "tablist", "aria-label": "Menu category" },
              categories.map((name) => h("button", {
                key: name,
                className: name === category ? "active" : "",
                onClick: () => setCategory(name)
              }, name))
            )
          ),
          h("div", { className: "menu-list" },
            filteredMenu.map((item) => h(MenuCard, { key: item.id, item, onAdd: addToCart }))
          )
        ),
        h("aside", { className: "checkout" },
          h("h2", null, "Cart"),
          h("div", { className: "cart-lines" },
            cartLines.length === 0
              ? h("p", { className: "muted" }, "Your cart is empty.")
              : cartLines.map((line) => h(CartLine, {
                  key: line.item.id,
                  line,
                  onChange: changeQuantity
                }))
          ),
          h("div", { className: "cart-total" },
            h("span", null, "Total"),
            h("strong", null, money(cartTotal))
          ),
          h("form", { onSubmit: submitOrder },
            h("label", null, "Name",
              h("input", {
                required: true,
                value: checkout.customerName,
                onChange: (event) => setCheckout({ ...checkout, customerName: event.target.value }),
                placeholder: "Priya"
              })
            ),
            h("label", null, "Delivery Address",
              h("input", {
                required: true,
                value: checkout.address,
                onChange: (event) => setCheckout({ ...checkout, address: event.target.value }),
                placeholder: "120 Spring Street"
              })
            ),
            h("label", null, "Notes",
              h("textarea", {
                value: checkout.notes,
                onChange: (event) => setCheckout({ ...checkout, notes: event.target.value }),
                placeholder: "No onions, call on arrival"
              })
            ),
            h("button", { className: "primary", type: "submit" }, "Place Order")
          )
        )
      ),
      h("section", { className: "orders" },
        h("div", { className: "section-head" },
          h("h2", null, "Kitchen Orders"),
          h("p", { className: "muted" }, `${orders.length} tickets`)
        ),
        h("div", { className: "order-list" },
          orders.map((order) => h(OrderCard, { key: order.id, order, onStatus: updateOrderStatus }))
        )
      )
    )
  );
}

function Metric({ label, value }) {
  return h("article", null, h("span", null, value), h("p", null, label));
}

function MenuCard({ item, onAdd }) {
  return h("article", { className: "menu-card" },
    h("div", null,
      h("p", { className: "category" }, item.category),
      h("h3", null, item.name),
      h("p", { className: "description" }, item.description)
    ),
    h("div", { className: "menu-actions" },
      h("strong", null, money(item.priceCents)),
      h("button", { onClick: () => onAdd(item) }, "Add")
    )
  );
}

function CartLine({ line, onChange }) {
  return h("div", { className: "cart-line" },
    h("div", null,
      h("strong", null, line.item.name),
      h("span", null, money(line.item.priceCents))
    ),
    h("div", { className: "stepper" },
      h("button", { type: "button", onClick: () => onChange(line.item.id, -1) }, "-"),
      h("span", null, line.quantity),
      h("button", { type: "button", onClick: () => onChange(line.item.id, 1) }, "+")
    )
  );
}

function OrderCard({ order, onStatus }) {
  return h("article", { className: "order-card" },
    h("div", { className: "order-top" },
      h("div", null,
        h("p", { className: "category" }, `Order #${order.id}`),
        h("h3", null, order.customerName),
        h("p", { className: "muted" }, order.address)
      ),
      h("span", { className: `status status-${order.status}` }, statusLabels[order.status])
    ),
    h("ul", null, order.items.map((item) => h("li", { key: item.menuItemId },
      h("span", null, `${item.quantity} x ${item.name}`),
      h("strong", null, money(item.subtotalCents))
    ))),
    order.notes && h("p", { className: "notes" }, order.notes),
    h("div", { className: "order-foot" },
      h("strong", null, money(order.totalCents)),
      h("div", { className: "status-actions" },
        nextStatuses.map((status) => h("button", {
          key: status,
          className: order.status === status ? "active" : "",
          onClick: () => onStatus(order.id, status)
        }, statusLabels[status]))
      )
    )
  );
}

ReactDOM.createRoot(document.querySelector("#root")).render(h(App));
