let session = JSON.parse(sessionStorage.getItem('pos.session') || 'null');
let onUnauthorized = null;

export const setOnUnauthorized = (cb) => { onUnauthorized = cb; };

export const getSession = () => session;

function createError(message, status) {
  const error = new Error(message);
  error.status = status;
  return error;
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (session?.token) {
    headers['Authorization'] = `Bearer ${session.token}`;
  }

  const res = await fetch(`/api${path}`, { ...options, headers });
  
  if (res.status === 401 && !path.includes('/auth/login')) {
    sessionStorage.removeItem('pos.session');
    session = null;
    if (onUnauthorized) onUnauthorized();
    throw createError('Nicht autorisiert', res.status);
  }

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    let msg = errorData.message || 'Ein Fehler ist aufgetreten';
    if (Array.isArray(errorData.errors) && errorData.errors.length > 0) {
      msg += ': ' + errorData.errors.join('; ');
    }
    throw createError(msg, res.status);
  }

  return res.json();
}

export const login = async (username, password) => {
  const data = await request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  });
  session = data;
  sessionStorage.setItem('pos.session', JSON.stringify(data));
  return data;
};

export const logout = () => {
  sessionStorage.removeItem('pos.session');
  session = null;
};

export const getProducts = () => request('/products/getAllProduct');
export const getInventories = () => request('/inventories/getAllInventories');
export const getCustomers = () => request('/customers/getAllCustomer');
export const createOrder = (body) => request('/orders/new-order', { method: 'POST', body: JSON.stringify(body) });
export const getOrders = () => request('/orders/getAllOrders');
export const cancelOrder = (id) => request(`/orders/${id}/cancel`, { method: 'POST' });
export const createPayment = (body) => request('/payments/new-payment', { method: 'POST', body: JSON.stringify(body) });
