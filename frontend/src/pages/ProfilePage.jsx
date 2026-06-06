import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api/client';
import { Modal } from '../components/Modal.jsx';

export default function ProfilePage() {
  const { user, refreshUser, logout } = useAuth();
  const [pwdOpen, setPwdOpen] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function submitPasswordReset() {
    setError('');
    setMessage('');
    setLoading(true);
    try {
      await apiFetch('/api/auth/change-password', {
        method: 'POST',
        body: JSON.stringify({ currentPassword, newPassword }),
      });
      setMessage('Password updated.');
      setCurrentPassword('');
      setNewPassword('');
      setPwdOpen(false);
      refreshUser();
    } catch (e) {
      setError(e.body?.message || e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page narrow">
      <h1>Profile</h1>
      <div className="card pad-lg stack gap-md">
        <div className="row gap-md align-start">
          <span className="avatar lg" aria-hidden="true">
            {user?.fullName?.charAt(0)?.toUpperCase() || '?'}
          </span>
          <div className="profile-intro">
            <h2 className="name-heading">{user?.fullName}</h2>
            <p className="muted small">Signed in session</p>
          </div>
        </div>
        <dl className="kv">
          <div>
            <dt>User ID</dt>
            <dd>{user?.userId}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{user?.email || '—'}</dd>
          </div>
          <div>
            <dt>Phone</dt>
            <dd>{user?.phoneNumber || '—'}</dd>
          </div>
        </dl>
        <div className="row gap-sm">
          <button type="button" className="btn secondary" onClick={() => setPwdOpen(true)}>
            Change password
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => logout()}>
            Log out
          </button>
        </div>
        {message ? <div className="banner success">{message}</div> : null}
        {error ? <div className="banner error">{error}</div> : null}
      </div>

      {pwdOpen ? (
        <Modal
          title="Change password"
          onClose={() => {
            setPwdOpen(false);
            setCurrentPassword('');
            setNewPassword('');
            setError('');
          }}
          footer={
            <div className="row gap-sm justify-end">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => {
                  setPwdOpen(false);
                  setCurrentPassword('');
                  setNewPassword('');
                  setError('');
                }}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn primary"
                disabled={loading}
                onClick={() => submitPasswordReset()}
              >
                {loading ? 'Saving…' : 'Reset'}
              </button>
            </div>
          }
        >
          <div className="stack gap-md">
            <label className="field">
              <span>Current password</span>
              <input
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
            </label>
            <label className="field">
              <span>New password</span>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                autoComplete="new-password"
                required
                minLength={6}
              />
            </label>
            {error ? <div className="banner error">{error}</div> : null}
          </div>
        </Modal>
      ) : null}
    </div>
  );
}
