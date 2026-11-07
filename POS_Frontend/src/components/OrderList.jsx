import { useState, useEffect, useCallback } from 'react';
import * as api from '../api';

export default function OrderList({ onError }) {
  const [orders, setOrders] = useState([]);
  const [loaded, setLoaded] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await api.getOrders();
      setOrders(data.sort((a,b) => new Date(b.orderDate) - new Date(a.orderDate)));
    } catch (e) { onError(e.message); } finally { setLoaded(true); }
  }, [onError]);

  useEffect(() => { load(); }, [load]);

  const handleAction = async (order, action) => {
    setLoading(true);
    try {
      if (action === 'cancel') await api.cancelOrder(order.id);
      if (action === 'pay') await api.createPayment({ orderId: order.id, amount: order.totalAmount });
      await load();
    } catch (e) { onError(e.message); } finally { setLoading(false); }
  };

  const statusMap = { 'OPEN': 'Offen', 'PAID': 'Bezahlt', 'CANCELLED': 'Storniert' };

  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Bestellnr.</th><th>Datum</th><th>Kunde</th><th>Summe</th><th>Status</th><th>Aktion</th></tr></thead>
        <tbody>
          {!loaded && <tr><td colSpan={6} className="muted">Bestellungen werden geladen...</td></tr>}
          {loaded && orders.length === 0 && <tr><td colSpan={6} className="muted">Keine Bestellungen vorhanden</td></tr>}
          {orders.map(o => (
            <tr key={o.id} data-testid="order-row">
              <td>{o.id}</td>
              <td>{new Date(o.orderDate).toLocaleDateString('de-DE')}</td>
              <td>{o.customer.fullName}</td>
              <td>{new Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(o.totalAmount)}</td>
              <td data-testid="order-status" className="status">{statusMap[o.status]}</td>
              <td>
                {o.status === 'OPEN' && (
                  <>
                    <button data-testid="pay-order" disabled={loading} onClick={() => handleAction(o, 'pay')}>Bezahlen</button>
                    <button data-testid="cancel-order" disabled={loading} onClick={() => handleAction(o, 'cancel')}>Stornieren</button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
