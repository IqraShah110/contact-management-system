import { NavLink } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from '../context/AuthContext';

export function AppShell({ children }) {
  const { user, logout } = useAuth();

  return (
    <div className="layout">
      <header className="topbar">
        <nav className="nav-links">
          <NavLink to="/contacts" end className={({ isActive }) => (isActive ? 'active' : '')}>
            Contacts
          </NavLink>
          <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : '')}>
            Profile
          </NavLink>
        </nav>
        <div className="user-chip">
          <span className="avatar" aria-hidden="true">
            {user?.fullName?.charAt(0)?.toUpperCase() || '?'}
          </span>
          <span>{user?.fullName}</span>
          <button type="button" className="btn btn-ghost sm" onClick={() => logout()}>
            Log out
          </button>
        </div>
      </header>
      <main className="main">{children}</main>
    </div>
  );
}

AppShell.propTypes = {
  children: PropTypes.node,
};
