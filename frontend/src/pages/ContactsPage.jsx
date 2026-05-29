import { useCallback, useEffect, useState } from 'react';
import { apiFetch } from '../api/client';
import { Modal } from '../components/Modal.jsx';
import { ContactForm } from '../components/ContactForm.jsx';
import {
  emptyContactForm,
  contactToForm,
  buildPayload,
} from '../components/contactForm.js';

function SearchToolbar({ searchInput, setSearchInput, onSearch, onClear }) {
  return (
    <div className="toolbar row gap-md wrap">
      <form
        className="row gap-sm grow"
        onSubmit={(e) => {
          e.preventDefault();
          onSearch();
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
        <button type="button" className="btn btn-ghost" onClick={onClear}>
          Clear
        </button>
      </form>
    </div>
  );
}

function ContactsTableCard({ loading, content, onOpenDetail, onEdit, onDelete }) {
  if (loading) {
    return (
      <div className="card pad-none table-wrap">
        <p className="pad-lg muted">Loading…</p>
      </div>
    );
  }

  if (content.length === 0) {
    return (
      <div className="card pad-none table-wrap">
        <p className="pad-lg muted">No contacts yet. Create one to get started.</p>
      </div>
    );
  }

  return (
    <div className="card pad-none table-wrap">
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
                <button type="button" className="linklike" onClick={() => onOpenDetail(row.id)}>
                  {row.firstName} {row.lastName}
                </button>
              </td>
              <td className="muted">{row.title || '—'}</td>
              <td className="actions row gap-sm justify-end">
                <button type="button" className="btn btn-ghost sm" onClick={() => onEdit(row)}>
                  Edit
                </button>
                <button type="button" className="btn danger sm ghost" onClick={() => onDelete(row.id)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function PaginationBar({ page, totalPages, onPrevious, onNext }) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="pager row gap-sm">
      <button type="button" className="btn btn-ghost" disabled={page <= 0} onClick={onPrevious}>
        Previous
      </button>
      <span className="muted small">
        Page {page + 1} of {totalPages}
      </span>
      <button type="button" className="btn btn-ghost" disabled={page + 1 >= totalPages} onClick={onNext}>
        Next
      </button>
    </div>
  );
}

function ContactProfileModal({ detail, onClose }) {
  if (!detail) {
    return null;
  }

  return (
    <Modal title="Contact profile" onClose={onClose}>
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
  );
}

function EditorModal({ title, form, setForm, onClose, onSave }) {
  return (
    <Modal
      title={title}
      onClose={onClose}
      footer={
        <div className="row gap-sm justify-end">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Cancel
          </button>
          <button type="button" className="btn primary" onClick={onSave}>
            Save
          </button>
        </div>
      }
    >
      <ContactForm form={form} setForm={setForm} />
    </Modal>
  );
}

function DeleteConfirmModal({ deleteId, onClose, onConfirm }) {
  if (!deleteId) {
    return null;
  }

  return (
    <Modal
      title="Delete contact?"
      onClose={onClose}
      footer={
        <div className="row gap-sm justify-end">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Cancel
          </button>
          <button type="button" className="btn danger" onClick={onConfirm}>
            Delete
          </button>
        </div>
      }
    >
      <p>This contact will be permanently removed.</p>
    </Modal>
  );
}

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

  function handleApiError(e) {
    setError(e.body?.message || e.message);
  }

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
      handleApiError(e);
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

      <SearchToolbar
        searchInput={searchInput}
        setSearchInput={setSearchInput}
        onSearch={() => {
          setPage(0);
          setSearchTerm(searchInput);
        }}
        onClear={() => {
          setSearchInput('');
          setSearchTerm('');
          setPage(0);
        }}
      />

      {error ? <div className="banner error">{error}</div> : null}

      <ContactsTableCard
        loading={loading}
        content={content}
        onOpenDetail={openDetail}
        onEdit={openEdit}
        onDelete={setDeleteId}
      />

      <PaginationBar
        page={page}
        totalPages={totalPages}
        onPrevious={() => setPage((p) => p - 1)}
        onNext={() => setPage((p) => p + 1)}
      />

      <ContactProfileModal detail={detail} onClose={() => setDetail(null)} />

      {createOpen ? (
        <EditorModal
          title="New contact"
          form={createForm}
          setForm={setCreateForm}
          onClose={() => setCreateOpen(false)}
          onSave={() => submitCreate().catch(handleApiError)}
        />
      ) : null}

      {editContact ? (
        <EditorModal
          title="Edit contact"
          form={editForm}
          setForm={setEditForm}
          onClose={() => setEditContact(null)}
          onSave={() => submitEdit().catch(handleApiError)}
        />
      ) : null}

      <DeleteConfirmModal
        deleteId={deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={() => confirmDelete().catch(handleApiError)}
      />
    </div>
  );
}
