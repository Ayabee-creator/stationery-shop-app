// ===== StudyGrid Admin Panel - API Utility =====

// CHANGE THIS to your deployed backend URL when hosting online
// Local development: 'http://localhost:8080'
// Railway example: 'https://studygrid-backend-production.up.railway.app'
const API_BASE = window.STUDYGRID_API || 'https://stationery-shop-app-production.up.railway.app';

async function apiGet(endpoint) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error(`GET ${endpoint} failed:`, error);
        throw error;
    }
}

async function apiPost(endpoint, data) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return await response.json();
    } catch (error) {
        console.error(`POST ${endpoint} failed:`, error);
        throw error;
    }
}

async function apiPut(endpoint, data) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return await response.json();
    } catch (error) {
        console.error(`PUT ${endpoint} failed:`, error);
        throw error;
    }
}

async function apiDelete(endpoint) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            method: 'DELETE'
        });
        return await response.json();
    } catch (error) {
        console.error(`DELETE ${endpoint} failed:`, error);
        throw error;
    }
}

function formatCurrency(amount) {
    return `R${parseFloat(amount).toFixed(2)}`;
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-ZA', { 
        day: 'numeric', month: 'short', year: 'numeric' 
    });
}

function getStatusBadge(status) {
    return `<span class="badge badge-${status}">${status}</span>`;
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed; bottom: 20px; right: 20px; padding: 12px 20px;
        background: ${type === 'success' ? '#10B981' : '#EF4444'}; color: white;
        border-radius: 8px; font-size: 14px; z-index: 9999;
        animation: slideIn 0.3s ease;
    `;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}
