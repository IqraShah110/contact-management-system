import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import PropTypes from 'prop-types';
import { useAuth } from './context/AuthContext';
import { AppShell } from './layout/AppShell';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import ContactsPage from './pages/ContactsPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';

function RequireAuth() {
  const { user, bootstrapping } = useAuth();

  if (bootstrapping) {
    return (
      <div className="page-center muted">
        <p>Checking session…</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <AppShell>
      <Outlet />
    </AppShell>
  );
}

function PublicOnly({ children }) {
  const { user, bootstrapping } = useAuth();
  if (bootstrapping) {
    return (
      <div className="page-center muted">
        <p>Checking session…</p>
      </div>
    );
  }
  if (user) {
    return <Navigate to="/contacts" replace />;
  }
  return children;
}

PublicOnly.propTypes = {
  children: PropTypes.node,
};

export default function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicOnly>
            <Login />
          </PublicOnly>
        }
      />
      <Route
        path="/register"
        element={
          <PublicOnly>
            <Register />
          </PublicOnly>
        }
      />

      <Route element={<RequireAuth />}>
        <Route path="/" element={<Navigate to="/contacts" replace />} />
        <Route path="/contacts" element={<ContactsPage />} />
        <Route path="/profile" element={<ProfilePage />} />
      </Route>

      <Route path="*" element={<Navigate to="/contacts" replace />} />
    </Routes>
  );
}
