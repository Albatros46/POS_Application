import { useState, useEffect, useCallback, useRef } from 'react';
import * as api from './api';
import LoginForm from './components/LoginForm';
import ProductGrid from './components/ProductGrid';
import Cart from './components/Cart';
import OrderList from './components/OrderList';
import './App.css';
import './index.css';

export default function App() {
  const [session, setSession] = useState(api.getSession());
  const [activeTab, setActiveTab] = useState('verkauf');
  const [error, setError] = useState(null);
  const errorTimer = useRef(null);

  useEffect(() => {
    api.setOnUnauthorized(() => setSession(null));
  }, []);

  const handleLogin = (user) => setSession(user);
  const handleLogout = () => {
    api.logout();
    setSession(null);
  };

  const showError = useCallback((msg) => {
    clearTimeout(errorTimer.current);
    setError(msg);
    errorTimer.current = setTimeout(() => setError(null), 5000);
  }, []);

  if (!session) {
    return (
      <div className="container">
        {error && <div role="alert" className="error" data-testid="error-message">{error}</div>}
        <LoginForm onLogin={handleLogin} onError={showError} />
      </div>
    );
  }

  return (
    <div className="app-layout">
      <header className="header">
        <h1>POS Kasse</h1>
        <div className="user-info">
          <span>{session.username} ({session.roles.join(', ')})</span>
          <button onClick={handleLogout} data-testid="logout">Abmelden</button>
        </div>
      </header>

      <nav className="tabs">
        <button className={activeTab === 'verkauf' ? 'active' : ''} onClick={() => setActiveTab('verkauf')}>Verkauf</button>
        <button className={activeTab === 'bestellungen' ? 'active' : ''} onClick={() => setActiveTab('bestellungen')}>Bestellungen</button>
      </nav>

      {error && <div role="alert" className="error" data-testid="error-message">{error}</div>}

      <main className="content">
        {activeTab === 'verkauf' ? (
          <div className="verkauf-grid">
            <ProductGrid onError={showError} />
          </div>
        ) : (
          <OrderList onError={showError} />
        )}
      </main>
    </div>
  );
}
