export const EMAIL_LABELS = ['WORK', 'PERSONAL', 'OTHER'];
export const PHONE_LABELS = ['WORK', 'HOME', 'PERSONAL', 'OTHER'];

export function emptyContactForm() {
  return {
    firstName: '',
    lastName: '',
    title: '',
    emails: [{ email: '', label: 'WORK' }],
    phones: [{ phoneNumber: '', label: 'WORK' }],
  };
}

export function contactToForm(contact) {
  const emails =
    contact.emails?.length > 0
      ? contact.emails.map((e) => ({ email: e.email, label: e.label }))
      : [{ email: '', label: 'WORK' }];
  const phones =
    contact.phones?.length > 0
      ? contact.phones.map((p) => ({ phoneNumber: p.phoneNumber, label: p.label }))
      : [{ phoneNumber: '', label: 'WORK' }];
  return {
    firstName: contact.firstName || '',
    lastName: contact.lastName || '',
    title: contact.title || '',
    emails,
    phones,
  };
}

export function buildPayload(form) {
  const emails = form.emails
    .filter((row) => row.email?.trim())
    .map((row) => ({ email: row.email.trim(), label: row.label }));

  const phones = form.phones
    .filter((row) => row.phoneNumber?.trim())
    .map((row) => ({ phoneNumber: row.phoneNumber.trim(), label: row.label }));

  return {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    title: form.title.trim() || null,
    emails,
    phones,
  };
}
