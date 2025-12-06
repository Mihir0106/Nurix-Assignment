import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { JobProvider } from '@/context/JobContext';
import Layout from '@/components/Layout';
import Dashboard from '@/pages/Dashboard';
import JobsPage from '@/pages/JobsPage';
import DLQPage from '@/pages/DLQPage';

function App() {
  return (
    <Router>
      <JobProvider>
        <Layout>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/jobs" element={<JobsPage />} />
            <Route path="/dlq" element={<DLQPage />} />
          </Routes>
        </Layout>
      </JobProvider>
    </Router>
  );
}

export default App;
