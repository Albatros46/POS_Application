import { useState } from 'react';
import * as api from '../api';

export default function LoginForm({ onLogin, onError }) {
  const [loading, setLoading] = useState(false);
  const [creds, setCreds] = useState({ username: '', password: '' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const user = await api.login(creds.username, creds.password);
      onLogin(user);
    } catch (err) {
      onError(err.status === 401 ? 'Benutzername oder Passwort ist falsch' : err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <h2>Anmeldung</h2>
      <label htmlFor="user">Benutzername</label>
      <input id="user" data-testid="login-username" value={creds.username} onChange={e => setCreds({...creds, username: e.target.value})} required />
      <label htmlFor="pass">Passwort</label>
      <input id="pass" data-testid="login-password" type="password" value={creds.password} onChange={e => setCreds({...creds, password: e.target.value})} required />
      <button type="submit" data-testid="login-submit" disabled={loading}>{loading ? 'Lädt...' : 'Anmelden'}</button>
    </form>
  );
}
