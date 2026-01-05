import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuration: 10 concurrent users, running for 30 seconds
export const options = {
    vus: 10,
    duration: '30s',
};

export default function () {
    // 1. POST Request: Shorten a URL
    const payload = JSON.stringify({
        url: 'https://www.google.com/search?q=k6-load-testing',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const shortenRes = http.post('http://localhost:8080/shorten', payload, params);

    // Check if shorten failed
    check(shortenRes, {
        'shorten status is 201': (r) => r.status === 201,
        'short_code exists': (r) => r.json('shortCode') !== undefined,
    });

    // Extract the short code for the redirect test
    // (Only try to redirect if the shorten request actually worked)
    if (shortenRes.status === 201) {
        const shortCode = shortenRes.json('shortCode');

        // 2. GET Request: Redirect
        // Note: k6 automatically follows redirects. We can set 'redirects: 0' if we want to measure just the 302 response time.
        // Here we let it follow to see full user experience, or blocking redirects to test server speed purely.

        // Testing the redirect endpoint performance
        const redirectRes = http.get(`http://localhost:8080/redirect?code=${shortCode}`, {
            redirects: 0 // Stop k6 from following the redirect so we measure YOUR server, not Google.com
        });

        check(redirectRes, {
            'redirect status is 302': (r) => r.status === 302,
            'location header is correct': (r) => r.headers['Location'] === 'https://www.google.com/search?q=k6-load-testing',
        });
    }

    // Slight pause between user iterations (simulates real user thinking time)
    sleep(1);
}