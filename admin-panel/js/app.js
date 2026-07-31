// ===== Stationery Za Admin Panel - Full App Logic =====

// ===== CONFIG =====
const ADMIN_USER = 'admin';
const ADMIN_PASS = 'admin123';
const REVENUE_GOAL = 2000; // Monthly goal in Rands
let refreshInterval = null;
let salesChart = null;
let analyticsChart = null;
let selectedOrders = new Set();

// ===== LOGIN =====
function handleLogin() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    if (username === ADMIN_USER && password === ADMIN_PASS) {
        sessionStorage.setItem('admin_logged_in', 'true');
        showApp();
    } else {
        showToast('Invalid credentials', 'error');
    }
}

function handleLogout() {
    sessionStorage.removeItem('admin_logged_in');
    document.getElementById('app-container').style.display = 'none';
    document.getElementById('login-page').style.display = 'flex';
    if (refreshInterval) clearInterval(refreshInterval);
}

function showApp() {
    document.getElementById('login-page').style.display = 'none';
    document.getElementById('app-container').style.display = 'flex';
    navigateTo('dashboard');
    startLiveRefresh();
}

function checkAuth() {
    if (sessionStorage.getItem('admin_logged_in') === 'true') {
        showApp();
    }
}

// ===== NAVIGATION =====
function navigateTo(page) {
    document.querySelectorAll('.page-section').forEach(el => el.classList.remove('active'));
    document.getElementById(`page-${page}`).classList.add('active');

    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    const navEl = document.querySelector(`[data-page="${page}"]`);
    if (navEl) navEl.classList.add('active');

    switch (page) {
        case 'dashboard': loadDashboard(); break;
        case 'orders': loadOrders(); break;
        case 'products': loadProducts(); break;
        case 'customers': loadCustomers(); break;
        case 'analytics': loadAnalytics(); break;
    }
}

// ===== LIVE REFRESH =====
function startLiveRefresh() {
    if (refreshInterval) clearInterval(refreshInterval);
    refreshInterval = setInterval(() => {
        const activePage = document.querySelector('.page-section.active');
        if (activePage && activePage.id === 'page-dashboard') {
            loadDashboard();
        }
    }, 30000); // Every 30 seconds
}

// ===== THEME TOGGLE =====
function toggleTheme() {
    const html = document.documentElement;
    const current = html.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    html.setAttribute('data-theme', next);
    document.getElementById('theme-icon').textContent = next === 'dark' ? '🌙' : '☀️';
    localStorage.setItem('admin-theme', next);
}

function loadTheme() {
    const saved = localStorage.getItem('admin-theme');
    if (saved) {
        document.documentElement.setAttribute('data-theme', saved);
        document.getElementById('theme-icon').textContent = saved === 'dark' ? '🌙' : '☀️';
    }
}

// ===== GLOBAL SEARCH =====
let searchData = { products: [], orders: [], customers: [] };

async function loadSearchData() {
    try {
        searchData.products = await apiGet('/products');
        searchData.orders = await apiGet('/admin/orders');
        searchData.customers = await apiGet('/admin/customers');
    } catch (e) {}
}

function globalSearch(query) {
    const container = document.getElementById('search-results');
    if (!query || query.length < 2) {
        container.classList.remove('active');
        return;
    }

    const q = query.toLowerCase();
    let results = [];

    searchData.products.filter(p => p.name.toLowerCase().includes(q)).slice(0, 3).forEach(p => {
        results.push({ type: 'Product', text: p.name, action: () => { navigateTo('products'); } });
    });

    searchData.orders.filter(o => ('#' + o.id).includes(q) || o.username.toLowerCase().includes(q)).slice(0, 3).forEach(o => {
        results.push({ type: 'Order', text: `#${o.id} - ${o.username}`, action: () => { navigateTo('orders'); } });
    });

    searchData.customers.filter(c => c.username.toLowerCase().includes(q) || c.email.toLowerCase().includes(q)).slice(0, 3).forEach(c => {
        results.push({ type: 'Customer', text: `${c.username} (${c.email})`, action: () => { navigateTo('customers'); } });
    });

    if (results.length === 0) {
        container.innerHTML = '<div class="search-result-item">No results found</div>';
    } else {
        container.innerHTML = results.map((r, i) => `
            <div class="search-result-item" onclick="searchResults[${i}]()">
                <span class="sr-type">${r.type}</span> ${r.text}
            </div>
        `).join('');
        window.searchResults = results.map(r => r.action);
    }
    container.classList.add('active');
}

document.addEventListener('click', (e) => {
    if (!e.target.closest('.global-search')) {
        document.getElementById('search-results').classList.remove('active');
    }
});

// ===== DASHBOARD =====
async function loadDashboard() {
    try {
        const stats = await apiGet('/admin/dashboard');
        document.getElementById('stat-revenue').textContent = formatCurrency(stats.total_revenue);
        document.getElementById('stat-orders').textContent = stats.total_orders;
        document.getElementById('stat-customers').textContent = stats.total_customers;
        document.getElementById('stat-products').textContent = stats.total_products;
        document.getElementById('stat-pending').textContent = stats.pending_orders;
        document.getElementById('stat-lowstock').textContent = stats.low_stock_count;

        // Revenue goal
        const monthData = await apiGet('/admin/revenue-month');
        const monthRev = monthData.month_revenue || 0;
        const percent = Math.min((monthRev / REVENUE_GOAL) * 100, 100);
        document.getElementById('goal-fill').style.width = percent + '%';
        document.getElementById('goal-text').textContent = `${formatCurrency(monthRev)} / ${formatCurrency(REVENUE_GOAL)}`;
        document.getElementById('goal-hint').textContent = percent >= 100 ? '🎉 Goal reached!' : `${Math.round(percent)}% of monthly goal`;

        // Sales chart
        loadSalesChart();

        // Activity feed
        loadActivityFeed();

        // Recent orders
        const orders = await apiGet('/admin/orders');
        let html = '';
        orders.slice(0, 5).forEach(o => {
            html += `<tr><td>#${o.id}</td><td>${o.username}</td><td>${formatCurrency(o.total_amount)}</td><td>${getStatusBadge(o.status)}</td><td>${formatDate(o.ordered_at)}</td></tr>`;
        });
        document.getElementById('recent-orders-body').innerHTML = html || '<tr><td colspan="5">No orders</td></tr>';

        // Top sellers
        loadTopSellers();

        // Load search data
        loadSearchData();
    } catch (error) {
        document.getElementById('stat-revenue').textContent = 'Error';
    }
}

async function loadSalesChart() {
    try {
        const data = await apiGet('/admin/revenue-chart');
        const labels = data.map(d => d.date.substring(5)); // MM-DD
        const revenues = data.map(d => d.revenue);

        const ctx = document.getElementById('salesChart').getContext('2d');
        if (salesChart) salesChart.destroy();

        salesChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Revenue (R)',
                    data: revenues,
                    borderColor: '#8B5CF6',
                    backgroundColor: 'rgba(139, 92, 246, 0.1)',
                    fill: true,
                    tension: 0.4,
                    borderWidth: 2,
                    pointRadius: 3,
                    pointBackgroundColor: '#8B5CF6'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(71,85,105,0.3)' }, ticks: { color: '#94A3B8', font: { size: 10 } } },
                    x: { grid: { display: false }, ticks: { color: '#94A3B8', font: { size: 10 } } }
                }
            }
        });
    } catch (e) {}
}

async function loadActivityFeed() {
    try {
        const activities = await apiGet('/admin/activity');
        let html = '';
        activities.forEach(a => {
            html += `
                <div class="activity-item">
                    <div class="activity-dot ${a.type}"></div>
                    <div>
                        <div class="activity-text">${a.message}</div>
                        <div class="activity-time">${formatDate(a.created_at)}</div>
                    </div>
                </div>
            `;
        });
        document.getElementById('activity-feed').innerHTML = html || '<p style="padding:20px;color:var(--text-muted)">No recent activity</p>';
    } catch (e) {
        document.getElementById('activity-feed').innerHTML = '<p style="padding:20px;color:var(--text-muted)">Failed to load</p>';
    }
}

async function loadTopSellers() {
    try {
        const products = await apiGet('/admin/top-products');
        if (products.length === 0) {
            document.getElementById('top-sellers').innerHTML = '<p style="padding:20px;color:var(--text-muted)">No sales data yet</p>';
            return;
        }
        const maxSold = products[0].total_sold;
        let html = '';
        products.slice(0, 5).forEach((p, i) => {
            const barWidth = (p.total_sold / maxSold) * 100;
            html += `
                <div class="top-seller-item">
                    <div class="top-seller-rank">${i + 1}</div>
                    <div class="top-seller-info">
                        <div class="top-seller-name">${p.name}</div>
                        <div class="top-seller-stat">${p.total_sold} sold · ${formatCurrency(p.total_revenue)}</div>
                    </div>
                    <div class="top-seller-bar"><div class="top-seller-bar-fill" style="width:${barWidth}%"></div></div>
                </div>
            `;
        });
        document.getElementById('top-sellers').innerHTML = html;
    } catch (e) {}
}

// ===== ORDERS =====
async function loadOrders() {
    try {
        const orders = await apiGet('/admin/orders');
        selectedOrders.clear();
        updateBulkBar();

        let html = '';
        orders.forEach(o => {
            html += `
                <tr>
                    <td><input type="checkbox" class="order-checkbox" value="${o.id}" onchange="toggleOrderSelect(${o.id})"></td>
                    <td><strong>#${o.id}</strong></td>
                    <td>${o.username}<br><small style="color:var(--text-muted)">${o.email}</small></td>
                    <td>${o.item_count} items</td>
                    <td><strong>${formatCurrency(o.total_amount)}</strong></td>
                    <td>${getStatusBadge(o.status)}</td>
                    <td>${formatDate(o.ordered_at)}</td>
                    <td>
                        <select class="form-control" style="width:110px;padding:4px 8px;font-size:11px;" onchange="updateOrderStatus(${o.id}, this.value)">
                            <option value="pending" ${o.status==='pending'?'selected':''}>Pending</option>
                            <option value="shipped" ${o.status==='shipped'?'selected':''}>Shipped</option>
                            <option value="delivered" ${o.status==='delivered'?'selected':''}>Delivered</option>
                        </select>
                        <button class="btn btn-outline btn-sm" style="margin-top:4px" onclick="showInvoice(${o.id})">Invoice</button>
                    </td>
                </tr>
            `;
        });
        document.getElementById('orders-table-body').innerHTML = html || '<tr><td colspan="8">No orders</td></tr>';
    } catch (error) {
        document.getElementById('orders-table-body').innerHTML = '<tr><td colspan="8">Failed to load</td></tr>';
    }
}

async function updateOrderStatus(orderId, newStatus) {
    try {
        await apiPut(`/admin/orders/${orderId}/status`, { status: newStatus });
        showToast(`Order #${orderId} → ${newStatus}`);
    } catch (e) { showToast('Failed', 'error'); }
}

function toggleOrderSelect(orderId) {
    if (selectedOrders.has(orderId)) selectedOrders.delete(orderId);
    else selectedOrders.add(orderId);
    updateBulkBar();
}

function toggleSelectAllOrders(checkbox) {
    const checkboxes = document.querySelectorAll('.order-checkbox');
    checkboxes.forEach(cb => {
        cb.checked = checkbox.checked;
        const id = parseInt(cb.value);
        if (checkbox.checked) selectedOrders.add(id);
        else selectedOrders.delete(id);
    });
    updateBulkBar();
}

function updateBulkBar() {
    const bar = document.getElementById('bulk-bar');
    if (selectedOrders.size > 0) {
        bar.style.display = 'flex';
        document.getElementById('bulk-count').textContent = selectedOrders.size + ' selected';
    } else {
        bar.style.display = 'none';
    }
}

async function bulkUpdateStatus(status) {
    if (selectedOrders.size === 0) return;
    try {
        await apiPut('/admin/orders/bulk-status', {
            status: status,
            order_ids: Array.from(selectedOrders)
        });
        showToast(`${selectedOrders.size} orders marked as ${status}`);
        loadOrders();
    } catch (e) { showToast('Bulk update failed', 'error'); }
}

function clearBulkSelection() {
    selectedOrders.clear();
    document.querySelectorAll('.order-checkbox').forEach(cb => cb.checked = false);
    document.getElementById('select-all-orders').checked = false;
    updateBulkBar();
}

// ===== INVOICE =====
async function showInvoice(orderId) {
    try {
        const order = await apiGet(`/orders/${orderId}`);
        let itemsHtml = '';
        if (order.items) {
            order.items.forEach(item => {
                itemsHtml += `<tr><td>${item.product_name}</td><td>${item.quantity}</td><td>${formatCurrency(item.unit_price)}</td><td>${formatCurrency(item.line_total)}</td></tr>`;
            });
        }

        document.getElementById('invoice-content').innerHTML = `
            <div style="display:flex;justify-content:space-between;margin-bottom:20px;">
                <div>
                    <h4>Stationery Za</h4>
                    <p style="color:var(--text-muted);font-size:12px;">Student Stationery Shop</p>
                </div>
                <div style="text-align:right;">
                    <p><strong>Invoice #${order.id}</strong></p>
                    <p style="font-size:12px;color:var(--text-muted)">${formatDate(order.ordered_at)}</p>
                </div>
            </div>
            <p><strong>Customer:</strong> ${order.username || 'N/A'}</p>
            <p><strong>Status:</strong> ${order.status}</p>
            <table style="width:100%;margin-top:16px;">
                <thead><tr><th>Product</th><th>Qty</th><th>Price</th><th>Total</th></tr></thead>
                <tbody>${itemsHtml}</tbody>
            </table>
            <div class="invoice-total">Total: ${formatCurrency(order.total_amount)}</div>
        `;
        document.getElementById('invoice-modal').classList.add('active');
    } catch (e) { showToast('Failed to load invoice', 'error'); }
}

function closeInvoiceModal() {
    document.getElementById('invoice-modal').classList.remove('active');
}

function printInvoice() {
    window.print();
}

// ===== PRODUCTS =====
let allProducts = [];
let categories = [];

async function loadProducts() {
    try {
        allProducts = await apiGet('/products');
        categories = await apiGet('/categories');
        renderProducts(allProducts);
    } catch (e) {
        document.getElementById('products-table-body').innerHTML = '<tr><td colspan="7">Failed to load</td></tr>';
    }
}

function renderProducts(products) {
    let html = '';
    products.forEach(p => {
        const img = p.image_url
            ? `<img src="${p.image_url}" class="product-img" alt="${p.name}" loading="lazy">`
            : `<div class="product-img" style="display:flex;align-items:center;justify-content:center;font-size:14px;">📦</div>`;

        const stockBadge = p.stock_quantity === 0
            ? `<span class="badge badge-low">Out!</span>`
            : p.stock_quantity < 30
                ? `<span class="badge badge-low">${p.stock_quantity}</span>`
                : `<span class="badge badge-ok">${p.stock_quantity}</span>`;

        const rowClass = p.stock_quantity === 0 ? 'class="out-of-stock"' : '';

        html += `
            <tr ${rowClass}>
                <td>${img}</td>
                <td><strong>${p.name}</strong><br><small style="color:var(--text-muted)">${(p.description || '').substring(0, 50)}</small></td>
                <td>${p.category}</td>
                <td><strong>${formatCurrency(p.price)}</strong></td>
                <td>${stockBadge} <button class="btn btn-restock" onclick="quickRestock(${p.id}, ${p.stock_quantity})">+50</button></td>
                <td>${p.avg_rating > 0 ? '⭐ ' + p.avg_rating : '-'}</td>
                <td>
                    <button class="btn btn-outline btn-sm" onclick="editProduct(${p.id})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteProduct(${p.id}, '${p.name.replace(/'/g, "\\'")}')">Del</button>
                </td>
            </tr>
        `;
    });
    document.getElementById('products-table-body').innerHTML = html || '<tr><td colspan="7">No products</td></tr>';
}

function filterProducts() {
    const query = document.getElementById('product-search').value.toLowerCase();
    const filtered = allProducts.filter(p =>
        p.name.toLowerCase().includes(query) || p.category.toLowerCase().includes(query)
    );
    renderProducts(filtered);
}

async function quickRestock(productId, currentStock) {
    try {
        await apiPut(`/admin/products/${productId}/stock`, { stock_quantity: currentStock + 50 });
        showToast('Added 50 units to stock');
        loadProducts();
    } catch (e) { showToast('Failed to restock', 'error'); }
}

function showAddProductModal() {
    document.getElementById('modal-title').textContent = 'Add New Product';
    document.getElementById('product-form').reset();
    document.getElementById('product-id').value = '';
    document.getElementById('image-preview').classList.remove('active');

    let options = '<option value="">Select Category</option>';
    categories.forEach(c => { options += `<option value="${c.id}">${c.name}</option>`; });
    document.getElementById('product-category').innerHTML = options;
    document.getElementById('product-modal').classList.add('active');
}

function editProduct(productId) {
    const product = allProducts.find(p => p.id === productId);
    if (!product) return;

    document.getElementById('modal-title').textContent = 'Edit Product';
    document.getElementById('product-id').value = product.id;
    document.getElementById('product-name').value = product.name;
    document.getElementById('product-description').value = product.description || '';
    document.getElementById('product-price').value = product.price;
    document.getElementById('product-stock').value = product.stock_quantity;
    document.getElementById('product-image').value = product.image_url || '';

    let options = '<option value="">Select Category</option>';
    categories.forEach(c => { options += `<option value="${c.id}" ${c.id === product.category_id ? 'selected' : ''}>${c.name}</option>`; });
    document.getElementById('product-category').innerHTML = options;

    previewImage(product.image_url);
    document.getElementById('product-modal').classList.add('active');
}

async function saveProduct() {
    const id = document.getElementById('product-id').value;
    const data = {
        name: document.getElementById('product-name').value,
        description: document.getElementById('product-description').value,
        category_id: parseInt(document.getElementById('product-category').value),
        price: parseFloat(document.getElementById('product-price').value),
        stock_quantity: parseInt(document.getElementById('product-stock').value) || 0,
        image_url: document.getElementById('product-image').value
    };

    if (!data.name || !data.category_id || !data.price) {
        showToast('Fill in required fields', 'error');
        return;
    }

    try {
        if (id) {
            await apiPut(`/admin/products/${id}`, data);
            showToast('Product updated!');
        } else {
            await apiPost('/admin/products', data);
            showToast('Product added!');
        }
        closeModal();
        loadProducts();
    } catch (e) { showToast('Save failed', 'error'); }
}

async function deleteProduct(productId, name) {
    if (!confirm(`Delete "${name}"? This cannot be undone.`)) return;
    try {
        await apiDelete(`/admin/products/${productId}`);
        showToast('Product deleted');
        loadProducts();
    } catch (e) { showToast('Delete failed', 'error'); }
}

function previewImage(url) {
    const preview = document.getElementById('image-preview');
    if (url && url.startsWith('http')) {
        preview.innerHTML = `<img src="${url}" alt="Preview" onerror="this.style.display='none'">`;
        preview.classList.add('active');
    } else {
        preview.classList.remove('active');
    }
}

function closeModal() {
    document.getElementById('product-modal').classList.remove('active');
}

// ===== CUSTOMERS =====
async function loadCustomers() {
    try {
        const customers = await apiGet('/admin/customers');
        let html = '';
        customers.forEach(c => {
            html += `
                <tr>
                    <td><strong>${c.username}</strong></td>
                    <td>${c.email}</td>
                    <td>${c.phone || '-'}</td>
                    <td>${c.order_count} orders</td>
                    <td><strong>${formatCurrency(c.total_spent)}</strong></td>
                    <td>${formatDate(c.created_at)}</td>
                </tr>
            `;
        });
        document.getElementById('customers-table-body').innerHTML = html || '<tr><td colspan="6">No customers</td></tr>';
    } catch (e) {
        document.getElementById('customers-table-body').innerHTML = '<tr><td colspan="6">Failed to load</td></tr>';
    }
}

// ===== ANALYTICS =====
async function loadAnalytics() {
    try {
        const monthData = await apiGet('/admin/revenue-month');
        document.getElementById('stat-month-revenue').textContent = formatCurrency(monthData.month_revenue || 0);
        document.getElementById('stat-month-orders').textContent = monthData.month_orders || 0;

        // Chart
        const chartData = await apiGet('/admin/revenue-chart');
        const labels = chartData.map(d => d.date.substring(5));
        const revenues = chartData.map(d => d.revenue);
        const orderCounts = chartData.map(d => d.orders);

        const ctx = document.getElementById('analyticsChart').getContext('2d');
        if (analyticsChart) analyticsChart.destroy();

        analyticsChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Revenue (R)',
                        data: revenues,
                        backgroundColor: 'rgba(139, 92, 246, 0.6)',
                        borderColor: '#8B5CF6',
                        borderWidth: 1,
                        borderRadius: 4,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Orders',
                        data: orderCounts,
                        type: 'line',
                        borderColor: '#06B6D4',
                        backgroundColor: 'rgba(6, 182, 212, 0.1)',
                        borderWidth: 2,
                        tension: 0.4,
                        pointRadius: 3,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { labels: { color: '#94A3B8', font: { size: 11 } } } },
                scales: {
                    y: { beginAtZero: true, position: 'left', grid: { color: 'rgba(71,85,105,0.3)' }, ticks: { color: '#94A3B8', font: { size: 10 } } },
                    y1: { beginAtZero: true, position: 'right', grid: { display: false }, ticks: { color: '#06B6D4', font: { size: 10 } } },
                    x: { grid: { display: false }, ticks: { color: '#94A3B8', font: { size: 10 } } }
                }
            }
        });

        // Top products table
        const topProducts = await apiGet('/admin/top-products');
        let topHtml = '';
        topProducts.forEach((p, i) => {
            topHtml += `
                <tr>
                    <td><strong>${i + 1}</strong></td>
                    <td>${p.name}</td>
                    <td>${p.category}</td>
                    <td>${p.total_sold} units</td>
                    <td><strong>${formatCurrency(p.total_revenue)}</strong></td>
                </tr>
            `;
        });
        document.getElementById('analytics-top-body').innerHTML = topHtml || '<tr><td colspan="5">No sales data</td></tr>';
    } catch (e) {}
}

// ===== INIT =====
document.addEventListener('DOMContentLoaded', () => {
    loadTheme();
    checkAuth();
});
