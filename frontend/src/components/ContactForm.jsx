import PropTypes from 'prop-types';
import { EMAIL_LABELS, PHONE_LABELS, rowKey } from './contactForm.js';

const emailRowShape = PropTypes.shape({
  email: PropTypes.string,
  label: PropTypes.string,
  _key: PropTypes.string,
});

const phoneRowShape = PropTypes.shape({
  phoneNumber: PropTypes.string,
  label: PropTypes.string,
  _key: PropTypes.string,
});

export function ContactForm({ form, setForm }) {
  function updateField(key, val) {
    setForm((f) => ({ ...f, [key]: val }));
  }

  function updateEmail(idx, patch) {
    setForm((f) => ({
      ...f,
      emails: f.emails.map((row, i) => (i === idx ? { ...row, ...patch } : row)),
    }));
  }

  function addEmail() {
    setForm((f) => ({
      ...f,
      emails: [...f.emails, { email: '', label: 'WORK', _key: rowKey('email') }],
    }));
  }

  function removeEmail(idx) {
    setForm((f) => ({
      ...f,
      emails: f.emails.length <= 1 ? f.emails : f.emails.filter((_, i) => i !== idx),
    }));
  }

  function updatePhone(idx, patch) {
    setForm((f) => ({
      ...f,
      phones: f.phones.map((row, i) => (i === idx ? { ...row, ...patch } : row)),
    }));
  }

  function addPhone() {
    setForm((f) => ({
      ...f,
      phones: [...f.phones, { phoneNumber: '', label: 'WORK', _key: rowKey('phone') }],
    }));
  }

  function removePhone(idx) {
    setForm((f) => ({
      ...f,
      phones: f.phones.length <= 1 ? f.phones : f.phones.filter((_, i) => i !== idx),
    }));
  }

  return (
    <div className="stack gap-md">
      <div className="row gap-md">
        <label className="field grow">
          <span>First name</span>
          <input value={form.firstName} onChange={(e) => updateField('firstName', e.target.value)} required />
        </label>
        <label className="field grow">
          <span>Last name</span>
          <input value={form.lastName} onChange={(e) => updateField('lastName', e.target.value)} required />
        </label>
      </div>
      <label className="field">
        <span>Title</span>
        <input value={form.title} onChange={(e) => updateField('title', e.target.value)} />
      </label>

      <fieldset className="fieldset">
        <legend>Emails</legend>
        {form.emails.map((row, idx) => (
          <div key={row._key} className="row gap-sm align-start">
            <label className="field grow">
              <span className="sr-only">Address</span>
              <input
                type="email"
                placeholder="name@company.com"
                value={row.email}
                onChange={(e) => updateEmail(idx, { email: e.target.value })}
              />
            </label>
            <label className="field narrow">
              <span className="sr-only">Label</span>
              <select value={row.label} onChange={(e) => updateEmail(idx, { label: e.target.value })}>
                {EMAIL_LABELS.map((l) => (
                  <option key={l} value={l}>
                    {l}
                  </option>
                ))}
              </select>
            </label>
            <button type="button" className="btn btn-ghost sm" onClick={() => removeEmail(idx)}>
              Remove
            </button>
          </div>
        ))}
        <button type="button" className="btn btn-ghost sm" onClick={addEmail}>
          + Email
        </button>
      </fieldset>

      <fieldset className="fieldset">
        <legend>Phones</legend>
        {form.phones.map((row, idx) => (
          <div key={row._key} className="row gap-sm align-start">
            <label className="field grow">
              <span className="sr-only">Number</span>
              <input
                placeholder="+1 ..."
                value={row.phoneNumber}
                onChange={(e) => updatePhone(idx, { phoneNumber: e.target.value })}
              />
            </label>
            <label className="field narrow">
              <span className="sr-only">Label</span>
              <select value={row.label} onChange={(e) => updatePhone(idx, { label: e.target.value })}>
                {PHONE_LABELS.map((l) => (
                  <option key={l} value={l}>
                    {l}
                  </option>
                ))}
              </select>
            </label>
            <button type="button" className="btn btn-ghost sm" onClick={() => removePhone(idx)}>
              Remove
            </button>
          </div>
        ))}
        <button type="button" className="btn btn-ghost sm" onClick={addPhone}>
          + Phone
        </button>
      </fieldset>
    </div>
  );
}

ContactForm.propTypes = {
  form: PropTypes.shape({
    firstName: PropTypes.string,
    lastName: PropTypes.string,
    title: PropTypes.string,
    emails: PropTypes.arrayOf(emailRowShape),
    phones: PropTypes.arrayOf(phoneRowShape),
  }).isRequired,
  setForm: PropTypes.func.isRequired,
};
