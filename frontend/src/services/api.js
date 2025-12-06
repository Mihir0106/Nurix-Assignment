import axios from 'axios';

const API_BASE_URL = '/api'; // Using proxy

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    // Add API key if needed
    config.headers['X-API-Key'] = 'dev-api-key-12345';
    // Add Tenant ID if needed (though often passed in body/params)
    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Handle global errors here
        console.error('API Error:', error);
        return Promise.reject(error);
    }
);

export const jobService = {
    submitJob: (payload, tenantId, idempotencyKey, maxRetries) => {
        return api.post('/jobs', {
            tenantId,
            payload,
            idempotencyKey,
            maxRetries
        });
    },
    listJobs: (tenantId, page = 0, size = 20, status) => {
        const params = { page, size };
        if (tenantId) params.tenantId = tenantId;
        if (status) params.status = status;
        return api.get('/jobs', { params });
    },
    getJob: (jobId) => api.get(`/jobs/${jobId}`),
    cancelJob: (jobId, tenantId) => api.delete(`/jobs/${jobId}`, { params: { tenantId } }),
    // Assuming DLQ endpoint exists or we fetch from jobs with filters
    // If specific DLQ endpoint doesn't exist, we might filter /jobs by FAILED
};

export default api;
