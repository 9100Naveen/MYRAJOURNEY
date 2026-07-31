<?php
declare(strict_types=1);

namespace Src\Utils;

class OllamaAI
{
    private string $baseUrl;
    private string $model;

    public function __construct()
    {
        $this->baseUrl = rtrim($_ENV['OLLAMA_BASE_URL'] ?? 'http://localhost:11434', '/');
        $this->model = $_ENV['OLLAMA_MODEL'] ?? 'llama3:latest';
    }

    public function getChatResponse(string $userMessage, ?array $context = null): string
    {
        $systemPrompt = $this->buildSystemPrompt($context);
        
        $data = [
            'model' => $this->model,
            'messages' => [
                [
                    'role' => 'system',
                    'content' => $systemPrompt
                ],
                [
                    'role' => 'user',
                    'content' => $userMessage
                ]
            ],
            'stream' => false,
            'options' => [
                'temperature' => 0.3,
                'num_predict' => 500
            ]
        ];

        $response = $this->makeRequest('/api/chat', $data);
        
        if (isset($response['message']['content'])) {
            return trim($response['message']['content']);
        }
        
        throw new \Exception('Invalid response structure from Ollama API');
    }

    private function buildSystemPrompt(?array $context = null): string
    {
        $prompt = "You are MyRA Assistant, an expert AI specialized in Rheumatoid Arthritis (RA) management, patient care, joint rehabilitation, DAS28 scoring, CRP metrics, and DMARD medication guidance.

IMPORTANT CLINICAL SAFETY GUIDELINES:
- Always emphasize consulting healthcare professionals/rheumatologists for medical decisions
- Provide accurate, evidence-based Rheumatoid Arthritis information
- Be empathetic, patient-centered, and supportive
- Keep responses concise, structured, and practical (under 300 words)";

        if ($context) {
            $prompt .= "\n\nPATIENT CONTEXT:";
            if (isset($context['user_name'])) {
                $prompt .= "\n- Patient Name: " . $context['user_name'];
            }
            if (isset($context['das28'])) {
                $prompt .= "\n- Recent DAS28 Score: " . $context['das28'];
            }
            if (isset($context['crp'])) {
                $prompt .= "\n- Recent CRP Level: " . $context['crp'] . " mg/L";
            }
            if (isset($context['symptoms'])) {
                $prompt .= "\n- Reported Symptoms: " . implode(', ', (array)$context['symptoms']);
            }
            if (isset($context['medications'])) {
                $prompt .= "\n- Current Medications: " . implode(', ', (array)$context['medications']);
            }
        }

        return $prompt;
    }

    private function makeRequest(string $endpoint, array $data): array
    {
        $url = $this->baseUrl . $endpoint;

        $ch = curl_init($url);
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST => true,
            CURLOPT_POSTFIELDS => json_encode($data),
            CURLOPT_HTTPHEADER => [
                'Content-Type: application/json'
            ],
            CURLOPT_TIMEOUT => 60,
            CURLOPT_CONNECTTIMEOUT => 5
        ]);

        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $error = curl_error($ch);
        
        curl_close($ch);

        if ($error) {
            throw new \Exception('Ollama connection failed: ' . $error . '. Ensure Ollama is running at ' . $this->baseUrl);
        }

        if ($httpCode !== 200) {
            throw new \Exception('Ollama API returned HTTP error ' . $httpCode);
        }

        $decoded = json_decode($response, true);
        
        if (json_last_error() !== JSON_ERROR_NONE) {
            throw new \Exception('Invalid JSON response from Ollama server');
        }

        return $decoded;
    }

    public function isConfigured(): bool
    {
        return !empty($this->baseUrl) && !empty($this->model);
    }
}
