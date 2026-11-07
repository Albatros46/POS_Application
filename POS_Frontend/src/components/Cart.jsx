import { useState, useEffect, useCallback } from 'react';
import * as api from '../api';

export default function Cart({ items, setItems, onOrderSuccess, onError, getStock }) {
  const [customers, setCustomers] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [activeOrder, setActiveOrder] = useState(null);
  const [paidOrderId, setPaidOrderId] = useState(null);
  const [loading, setLoading] = useState(false);

  const loadCustomers = useCallback(() => {
    api.getCustomers().then(setCustomers).catch(e => onError(e.message));
  }, [onError]);

  useEffect(() => { loadCustomers(); }, [loadCustomers]);

  // Adding an item ends the "bezahlt" confirmation.
  useEffect(() => { setPaidOrderId(null); }, [items]);

  const totalCents = items.reduce((sum, item) => sum + (Math.round(item.product.price * 100) * item.quantity), 0);

  const decrease = (productId) => setItems(prev => prev.flatMap(i => {
    if (i.product.id !== productId) return [i];
    return i.quantity > 1 ? [{ ...i, quantity: i.quantity - 1 }] : [];
  }));

  const increase = (productId) => setItems(prev => prev.map(i =>
    i.product.id === productId && i.quantity < getStock(productId) ? { ...i, quantity: i.quantity + 1 } : i
  ));

  const remove = (productId) => setItems(prev => prev.filter(i => i.product.id !== productId));

  const placeOrder = async () => {
    if (!selectedCustomer) return onError('Bitte wählen Sie einen Kunden');
    setLoading(true);
    try {
      const order = await api.createOrder({
        customerId: Number(selectedCustomer),
        items: items.map(i => ({ productId: i.product.id, quantity: i.quantity }))
      });
      setActiveOrder(order);
      setItems([]);
      onOrderSuccess();
    } catch (e) { onError(e.message); } finally { setLoading(false); }
  };

  const handlePay = async () => {
    setLoading(true);
    try {
      await api.createPayment({ orderId: activeOrder.id, amount: activeOrder.totalAmount });
      setPaidOrderId(activeOrder.id);
      setActiveOrder(null);
    } catch (e) { onError(e.message); } finally { setLoading(false); }
  };

  const handleCancel = async () => {
    setLoading(true);
    try {
      await api.cancelOrder(activeOrder.id);
      setActiveOrder(null);
      onOrderSuccess();
    } catch (e) { onError(e.message); } finally { setLoading(false); }
  };

  if (activeOrder) {
    return (
      <div className="cart-panel">
        <h3>Bestellung erstellt</h3>
        <p>Betrag: {new Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(activeOrder.totalAmount)}</p>
        <button data-testid="pay-order" onClick={handlePay} disabled={loading}>Bezahlen</button>
        <button data-testid="cancel-order" onClick={handleCancel} disabled={loading}>Stornieren</button>
      </div>
    );
  }

  return (
    <div className="cart-panel">
      <h3>Warenkorb</h3>
      {paidOrderId && <p role="status" className="status">Bestellung {paidOrderId} bezahlt</p>}
      <label htmlFor="customer-select">Kunde</label>
      <select id="customer-select" data-testid="customer-select" value={selectedCustomer} onChange={e => setSelectedCustomer(e.target.value)}>
        <option value="">Kunde wählen...</option>
        {customers.map(c => <option key={c.id} value={c.id}>{c.fullName}</option>)}
      </select>
      {items.length === 0 ? (
        <p className="muted">Leerer Warenkorb</p>
      ) : (
        <ul className="cart-list">
          {items.map(item => {
            const name = item.product.name;
            return (
              <li key={item.product.id} className="cart-line">
                <span>{name} x {item.quantity}</span>
                <div className="cart-line-actions">
                  <button className="qty-btn" aria-label={`Menge von ${name} verringern`} onClick={() => decrease(item.product.id)}>−</button>
                  <button className="qty-btn" aria-label={`Menge von ${name} erhöhen`} disabled={item.quantity >= getStock(item.product.id)} onClick={() => increase(item.product.id)}>+</button>
                  <button className="remove-btn" aria-label={`${name} entfernen`} onClick={() => remove(item.product.id)}>Entfernen</button>
                </div>
              </li>
            );
          })}
        </ul>
      )}
      <div className="cart-total" data-testid="cart-total">Summe: {new Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(totalCents / 100)}</div>
      <button data-testid="create-order" onClick={placeOrder} disabled={loading || items.length === 0}>Bestellung anlegen</button>
    </div>
  );
}
