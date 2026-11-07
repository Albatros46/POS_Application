import { useState, useEffect, useCallback } from 'react';
import * as api from '../api';
import Cart from './Cart';

export default function ProductGrid({ onError }) {
  const [products, setProducts] = useState([]);
  const [inventories, setInventories] = useState([]);
  const [loaded, setLoaded] = useState(false);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [cartItems, setCartItems] = useState([]);

  const fetchData = useCallback(async () => {
    try {
      const [p, i] = await Promise.all([api.getProducts(), api.getInventories()]);
      setProducts(p);
      setInventories(i);
    } catch (e) { onError(e.message); } finally { setLoaded(true); }
  }, [onError]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const getStock = (productId) => {
    const inv = inventories.find(i => i.product.id === productId);
    return inv ? inv.stockQuantity : 0;
  };

  const categories = [...new Set(products.map(p => p.category?.name).filter(Boolean))];

  const addToCart = (product) => {
    const stock = getStock(product.id);
    const current = cartItems.find(item => item.product.id === product.id)?.quantity || 0;

    if (current < stock) {
      setCartItems(prev => {
        const existing = prev.find(i => i.product.id === product.id);
        if (existing) return prev.map(i => i.product.id === product.id ? { ...i, quantity: i.quantity + 1 } : i);
        return [...prev, { product, quantity: 1 }];
      });
    }
  };

  const filteredProducts = products.filter(p =>
    p.name.toLowerCase().includes(search.toLowerCase()) &&
    (category === '' || p.category?.name === category)
  );

  return (
    <div className="split-view">
      <div className="product-column">
        <h3>Produkte</h3>
        <input placeholder="Suche..." value={search} onChange={e => setSearch(e.target.value)} aria-label="Produktsuche" />
        <select value={category} onChange={e => setCategory(e.target.value)} aria-label="Kategorie filtern">
          <option value="">Alle Kategorien</option>
          {categories.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
        <div className="product-list">
          {!loaded && <p className="muted">Produkte werden geladen...</p>}
          {loaded && filteredProducts.length === 0 && <p className="muted">Keine Produkte gefunden</p>}
          {filteredProducts.map(p => {
            const stock = getStock(p.id);
            const inCart = cartItems.find(item => item.product.id === p.id)?.quantity || 0;
            return (
              <div key={p.id} className="product-card" data-testid="product-card">
                <span>{p.name} - {new Intl.NumberFormat('de-DE', {style: 'currency', currency: 'EUR'}).format(p.price)}</span>
                <small>Lager: {stock}</small>
                <button data-testid="add-to-cart" disabled={inCart >= stock} onClick={() => addToCart(p)}>In den Warenkorb</button>
              </div>
            );
          })}
        </div>
      </div>
      <Cart items={cartItems} setItems={setCartItems} onOrderSuccess={fetchData} onError={onError} getStock={getStock} />
    </div>
  );
}
