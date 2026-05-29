import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    setError('');
    const emailTrim = email.trim();
    const phoneTrim = phoneNumber.trim();
    if (!emailTrim && !phoneTrim) {
      setError('Provide either an email address or a phone number.');
      return;
    }

    const body = {
      fullName: fullName.trim(),
      password,
    };
    if (emailTrim) body.email = emailTrim;
    if (phoneTrim) body.phoneNumber = phoneTrim;

    setLoading(true);
    try {
      await register(body);
      navigate('/login', { replace: true });
    } catch (err) {
      setError(err.body?.message || err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="card auth-card wide">
        <h1>Create account</h1>
        <p className="muted small">Register using email <strong>or</strong> phone (at least one required).</p>
        <form onSubmit={onSubmit} className="stack gap-md">
          <label className="field">
            <span>Full name</span>
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </label>
          <label className="field">
            <span>Email (optional)</span>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          </label>
          <label className="field">
            <span>Phone number (optional)</span>
            <input value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} />
          </label>
          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="new-password"
              required
              minLength={6}
            />
          </label>
          {error ? <div className="banner error">{error}</div> : null}
          <button type="submit" className="btn primary block" disabled={loading}>
            {loading ? 'Creating account…' : 'Register'}
          </button>
        </form>
        <p className="muted small footer-link">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
