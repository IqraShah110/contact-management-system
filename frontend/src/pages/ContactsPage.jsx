import { useCallback, useEffect, useState } from 'react';
import { apiFetch } from '../api/client';
import { Modal } from '../components/Modal.jsx';
import { ContactForm } from '../components/ContactForm.jsx';
import {
  emptyContactForm,
  contactToForm,
  buildPayload,
} from '../components/contactForm.js';

export default function ContactsPage() {
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [list, setList] = useState(null);

  const [detail, setDetail] = useState(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState(() => emptyContactForm());
  const [editContact, setEditContact] = useState(null);
  const [editForm, setEditForm] = useState(() => emptyContactForm());
  const [deleteId, setDeleteId] = useState(null);

  const load = useCallback(async () => {
    setError('');
    setLoading(true);
    try {
      const q = new URLSearchParams({ page: String(page), size: String(size) });
      const path = searchTerm.trim()
        ? `/api/contacts/search?searchTerm=${encodeURIComponent(searchTerm.trim())}&${q}`
        : `/api/contacts?${q}`;
      const data = await apiFetch(path);
      setList(data);
    } catch (e) {
      setError(e.body?.message || e.message);
      setList(null);
    } finally {
      setLoading(false);
    }
  }, [page, size, searchTerm]);

  useEffect(() => {
    load();
  }, [load]);

  function openCreate() {
    setCreateForm(emptyContactForm());
    setCreateOpen(true);
  }

  async function submitCreate() {
    const payload = buildPayload(createForm);
    await apiFetch('/api/contacts', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    setCreateOpen(false);
    setPage(0);
    load();
  }

  function openEdit(row) {
    setEditContact(row);
    setEditForm(contactToForm(row));
  }

  async function submitEdit() {
    const payload = buildPayload(editForm);
    await apiFetch(`/api/contacts/${editContact.id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
    setEditContact(null);
    load();
  }

  async function confirmDelete() {
    await apiFetch(`/api/contacts/${deleteId}`, { method: 'DELETE' });
    setDeleteId(null);
    load();
  }

  async function openDetail(id) {
    try {
      const row = await apiFetch(`/api/contacts/${id}`);
      setDetail(row);
    } catch (e) {
      setError(e.body?.message || e.message);
    }
  }

  const totalPages = list?.totalPages ?? 0;
  const content = list?.content ?? [];

  return (
    <div className="page">
      <div className="page-header row spread">
        <div>
          <h1>Contacts</h1>
          <p className="muted small">Search by first or last name, manage your address book.</p>
        </div>
        <button type="button" className="btn primary" onClick={openCreate}>
          New contact
        </button>
      </div>

      <div className="toolbar row gap-md wrap">
        <form
          className="row gap-sm grow"
          onSubmit={(e) => {
            e.preventDefault();
            setPage(0);
            setSearchTerm(searchInput);
          }}
        >
          <input
            className="grow"
            placeholder="Search first or last name…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <button type="submit" className="btn secondary">
            Search
          </button>
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => {
              setSearchInput('');
              setSearchTerm('');
              setPage(0);
            }}
          >
            Clear
          </button>
        </form>
      </div>

      {error ? <div className="banner error">{error}</div> : null}

      <div className="card pad-none table-wrap">
        {loading ? <p className="pad-lg muted">Loading…</p> : null}
        {!loading && content.length === 0 ? (
          <p className="pad-lg muted">No contacts yet. Create one to get started.</p>
        ) : null}
        {!loading && content.length > 0 ? (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Title</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {content.map((row) => (
                <tr key={row.id}>
                  <td>
                    <button type="button" className="linklike" onClick={() => openDetail(row.id)}>
                      {row.firstName} {row.lastName}
                    </button>
                  </td>
                  <td className="muted">{row.title || '—'}</td>
                  <td className="actions row gap-sm justify-end">
                    <button type="button" className="btn btn-ghost sm" onClick={() => openEdit(row)}>
                      Edit
                    </button>
                    <button type="button" className="btn danger sm ghost" onClick={() => setDeleteId(row.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : null}
      </div>

      {totalPages > 1 ? (
        <div className="pager row gap-sm">
          <button type="button" className="btn btn-ghost" disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span className="muted small">
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            className="btn btn-ghost"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            Next
          </button>
        </div>
      ) : null}

      {detail ? (
        <Modal title="Contact profile" onClose={() => setDetail(null)}>
          <div className="stack gap-md">
            <div>
              <h3 className="name-heading">
                {detail.firstName} {detail.lastName}
              </h3>
              {detail.title ? <p className="muted">{detail.title}</p> : null}
            </div>
            <section>
              <h4 className="section-title">Emails</h4>
              {detail.emails?.length ? (
                <ul className="muted list">
                  {detail.emails.map((e) => (
                    <li key={e.id ?? e.email}>
                      {e.email} ({e.label})
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="muted small">None</p>
              )}
            </section>
            <section>
              <h4 className="section-title">Phones</h4>
              {detail.phones?.length ? (
                <ul className="muted list">
                  {detail.phones.map((p) => (
                    <li key={p.id ?? p.phoneNumber}>
                      {p.phoneNumber} ({p.label})
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="muted small">None</p>
              )}
            </section>
            <p className="muted xs">
              Created {detail.createdAt ? new Date(detail.createdAt).toLocaleString() : '—'} · Updated{' '}
              {detail.updatedAt ? new Date(detail.updatedAt).toLocaleString() : '—'}
            </p>
          </div>
        </Modal>
      ) : null}

      {createOpen ? (
        <Modal
          title="New contact"
          onClose={() => setCreateOpen(false)}
          footer={
            <div className="row gap-sm justify-end">
              <button type="button" className="btn btn-ghost" onClick={() => setCreateOpen(false)}>
                Cancel
              </button>
              <button
                type="button"
                className="btn primary"
                onClick={() => submitCreate().catch((e) => setError(e.body?.message || e.message))}
              >
                Save
              </button>
            </div>
          }
        >
          <ContactForm form={createForm} setForm={setCreateForm} />
        </Modal>
      ) : null}

      {editContact ? (
        <Modal
          title="Edit contact"
          onClose={() => setEditContact(null)}
          footer={
            <div className="row gap-sm justify-end">
              <button type="button" className="btn btn-ghost" onClick={() => setEditContact(null)}>
                Cancel
              </button>
              <button
                type="button"
                className="btn primary"
                onClick={() => submitEdit().catch((e) => setError(e.body?.message || e.message))}
              >
                Save
              </button>
            </div>
          }
        >
          <ContactForm form={editForm} setForm={setEditForm} />
        </Modal>
      ) : null}

      {deleteId != null ? (
        <Modal
          title="Delete contact?"
          onClose={() => setDeleteId(null)}
          footer={
            <div className="row gap-sm justify-end">
              <button type="button" className="btn btn-ghost" onClick={() => setDeleteId(null)}>
                Cancel
              </button>
              <button
                type="button"
                className="btn danger"
                onClick={() => confirmDelete().catch((e) => setError(e.body?.message || e.message))}
              >
                Delete
              </button>
            </div>
          }
        >
          <p>This contact will be permanently removed.</p>
        </Modal>
      ) : null}
    </div>
  );
}
