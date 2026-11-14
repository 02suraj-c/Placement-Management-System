export const COMPANY_QUESTIONS = [
  {
    id: 1,
    qNum: 1,
    company: 'Tata Consultancy Services (TCS)',
    companyShort: 'TCS',
    topic: 'Java & Data Structures',
    targetRole: 'TCS Digital Software Engineer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900, // 15 mins
    isDsa: false,
    question:
      'Explain the internal mechanics of HashMap in Java 8. How are bucket collisions resolved using Red-Black Trees when TREEIFY_THRESHOLD is met, and what is the amortized search complexity?',
    constraints: [
      'Java 8+ specifications',
      'TREEIFY_THRESHOLD = 8, UNTREEIFY_THRESHOLD = 6, MIN_TREEIFY_CAPACITY = 64',
      'Explain rehashing, load factor (0.75), and treeify conditions',
    ],
    solutionExplanation:
      'In Java 8, java.util.HashMap uses an array of Node<K,V> buckets. When hash collisions occur, items were historically stored in a singly linked list with O(N) lookup. In Java 8, if a single bucket contains >= 8 items (TREEIFY_THRESHOLD) AND the total table capacity is >= 64, the bucket is transformed into a balanced Red-Black Tree (TreeNode<K,V>). This improves worst-case collision lookup from O(N) down to O(log N). During resizing or node removals, if the node count drops to <= 6, it reverts to a linked list.',
    solutionsByLang: {
      java: `// Java 8 HashMap Treeify Demo & Configuration
package com.placement.demo;

import java.util.HashMap;
import java.util.Map;

public class HashMapCollisionAnalysis {
    public static void main(String[] args) {
        // Default initial capacity is 16, load factor 0.75
        Map<BadHashKey, String> map = new HashMap<>(64);

        for (int i = 0; i < 12; i++) {
            map.put(new BadHashKey(i), "Val-" + i);
        }
        // At 8 collisions in capacity 64+, bucket converts to TreeNode (Red-Black Tree)
        System.out.println("Bucket size: " + map.size() + " [Lookup O(log N)]");
    }

    static class BadHashKey {
        int id;
        BadHashKey(int id) { this.id = id; }
        @Override public int hashCode() { return 42; } // Forced collision
        @Override public boolean equals(Object o) {
            return (o instanceof BadHashKey) && ((BadHashKey) o).id == this.id;
        }
    }
}`,
      python: `# Python dict equivalent uses open addressing with quadratic perturbation
# Lookup remains amortized O(1), collision chain perturbation:
class DictProbeSimulation:
    def __init__(self, size=64):
        self.size = size
        self.table = [None] * size

    def insert(self, key, value):
        h = hash(key)
        idx = h % self.size
        perturb = h
        while self.table[idx] is not None and self.table[idx][0] != key:
            idx = (5 * idx + 1 + perturb) % self.size
            perturb >>= 5
        self.table[idx] = (key, value)`,
      cpp: `// C++ std::unordered_map uses linked-list buckets (no automatic treeification in stdlib)
// Boost.Unordered or custom R-B tree bucket alternative:
#include <iostream>
#include <unordered_map>

struct CustomHash {
    std::size_t operator()(int x) const { return 42; } // Forced collision
};

int main() {
    std::unordered_map<int, std::string, CustomHash> map;
    for (int i = 0; i < 10; ++i) {
        map[i] = "Val-" + std::to_string(i);
    }
    std::cout << "Buckets: " << map.bucket_count() << std::endl;
    return 0;
}`,
      javascript: `// JavaScript Map uses deterministic insertion-order hash tables
const map = new Map();
for (let i = 0; i < 10; i++) {
  map.set(\`key_\${i}\`, \`value_\${i}\`);
}
console.log('Size:', map.size);
console.log('Amortized lookup:', 'O(1)');`,
    },
    starterCode: `// Java 21 - Implement a simplified Chained Hash Table with Treeify Threshold
public class Solution {
    public static int getWorstCaseLookup(int bucketSize, boolean isTreeified) {
        // Return 1 for O(1) representation, or logarithmic representation
        return isTreeified ? (int)(Math.log(bucketSize) / Math.log(2)) : bucketSize;
    }
}`,
    testCases: [
      { input: 'bucketSize = 8, isTreeified = true', expected: '3' },
      { input: 'bucketSize = 16, isTreeified = true', expected: '4' },
      { input: 'bucketSize = 5, isTreeified = false', expected: '5' },
    ],
  },
  {
    id: 2,
    qNum: 2,
    company: 'Infosys',
    companyShort: 'Infosys',
    topic: 'Database Management & SQL',
    targetRole: 'Infosys Power Programmer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Write an ANSI SQL query to retrieve the N-th highest employee salary across distinct departments without using LIMIT. Explain how composite indexes optimize this.',
    constraints: [
      'Must NOT use LIMIT or TOP syntax',
      'Must handle tie salaries correctly using DENSE_RANK()',
      'Explain B-Tree composite index on (department_id, salary DESC)',
    ],
    solutionExplanation:
      'The ANSI standard solution uses the DENSE_RANK() window function partitioned by department. Unlike RANK() which skips numbers on ties, DENSE_RANK() produces continuous rank integers (1, 2, 2, 3). Placing a composite index on (department_id, salary DESC) enables an Index Only Scan avoiding expensive disk sorting.',
    solutionsByLang: {
      java: `// JDBC / JPA Query with DENSE_RANK()
@Query(value = """
    WITH RankedSalaries AS (
        SELECT id, name, department_id, salary,
               DENSE_RANK() OVER (PARTITION BY department_id ORDER BY salary DESC) as rnk
        FROM employees
    )
    SELECT * FROM RankedSalaries WHERE rnk = :n
""", nativeQuery = true)
List<Employee> findNthHighestSalaryPerDept(@Param("n") int n);`,
      cpp: `// SQL formulation string for SQLite/MySQL/PostgreSQL C++ connector
const char* query = 
    "WITH RankedSalaries AS ("
    "  SELECT emp_id, salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk "
    "  FROM employees"
    ") "
    "SELECT salary FROM RankedSalaries WHERE rnk = ?;";`,
      python: `# Python Pandas / SQLAlchemy implementation
import pandas as pd

def nth_highest_salary(employees: pd.DataFrame, N: int) -> pd.DataFrame:
    unique_salaries = employees['salary'].drop_duplicates().sort_values(ascending=False)
    if len(unique_salaries) < N:
        return pd.DataFrame({'getNthHighestSalary': [None]})
    return pd.DataFrame({'getNthHighestSalary': [unique_salaries.iloc[N - 1]]})`,
      javascript: `// SQL Query in Node.js pg client
const query = \`
  WITH Ranked AS (
    SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) as rank_no
    FROM employees
  )
  SELECT DISTINCT salary FROM Ranked WHERE rank_no = $1;
\`;`,
    },
    starterCode: `// Write ANSI SQL or Java equivalent
public class Solution {
    public static String getSqlStatement(int n) {
        return "WITH Ranked AS (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) as rnk FROM employees) SELECT DISTINCT salary FROM Ranked WHERE rnk = " + n + ";";
    }
}`,
    testCases: [
      { input: 'N = 2, Salaries = [100, 200, 300, 200]', expected: '200' },
      { input: 'N = 1, Salaries = [5000, 3000, 8000]', expected: '8000' },
      { input: 'N = 4, Salaries = [100, 200]', expected: 'null' },
    ],
  },
  {
    id: 3,
    qNum: 3,
    company: 'HCLTech',
    companyShort: 'HCLTech',
    topic: 'Operating Systems',
    targetRole: 'Graduate Engineer Trainee',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Detail the 4 Coffman conditions required for a Deadlock in an Operating System. How does lock hierarchy ordering mathematically prevent Circular Wait?',
    constraints: [
      'Coffman conditions: Mutual Exclusion, Hold & Wait, No Preemption, Circular Wait',
      'Provide concrete Java concurrency lock-ordering snippet',
    ],
    solutionExplanation:
      'Deadlocks require 4 simultaneous conditions: 1) Mutual Exclusion (exclusive resource holding), 2) Hold and Wait (process holding resource requests others), 3) No Preemption (resources cannot be forcibly reclaimed), 4) Circular Wait (chain P0->P1->...->P0). Enforcing a global strict partial order on resource IDs (always acquiring Lock(min(A,B)) before Lock(max(A,B))) eliminates cycles, rendering Circular Wait mathematically impossible.',
    solutionsByLang: {
      java: `// Lock Ordering Prevention Protocol
public class SafeTransfer {
    public void transfer(Account from, Account to, double amount) {
        Account first = from.getId() < to.getId() ? from : to;
        Account second = from.getId() < to.getId() ? to : from;

        synchronized (first) {
            synchronized (second) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }
}`,
      cpp: `// C++11 std::lock avoids deadlocks automatically via deadlock-avoidance algorithm
#include <mutex>

void safeTransfer(std::mutex& m1, std::mutex& m2) {
    // std::lock acquires all mutexes simultaneously without deadlock
    std::lock(m1, m2);
    std::lock_guard<std::mutex> lock1(m1, std::adopt_lock);
    std::lock_guard<std::mutex> lock2(m2, std::adopt_lock);
    // critical section
}`,
      python: `# Python ContextManager Lock Ordering
from contextlib import contextmanager

@contextmanager
def acquire_locks(*locks):
    # Sort locks by unique id to prevent circular wait
    sorted_locks = sorted(locks, key=lambda l: id(l))
    for lock in sorted_locks:
        lock.acquire()
    try:
        yield
    finally:
        for lock in reversed(sorted_locks):
            lock.release()`,
      javascript: `// Node.js async resource ordering
async function executeInOrder(resA, resB, task) {
  const [first, second] = resA.id < resB.id ? [resA, resB] : [resB, resA];
  await first.lock();
  await second.lock();
  try {
    return await task();
  } finally {
    second.unlock();
    first.unlock();
  }
}`,
    },
    starterCode: `public class Solution {
    public static boolean checkDeadlockCondition(boolean mutEx, boolean holdWait, boolean noPreempt, boolean circWait) {
        return mutEx && holdWait && noPreempt && circWait;
    }
}`,
    testCases: [
      { input: 'mutEx=true, holdWait=true, noPreempt=true, circWait=true', expected: 'true' },
      { input: 'mutEx=true, holdWait=true, noPreempt=true, circWait=false', expected: 'false' },
      { input: 'mutEx=false, holdWait=true, noPreempt=true, circWait=true', expected: 'false' },
    ],
  },
  {
    id: 4,
    qNum: 4,
    company: 'Cisco Systems',
    companyShort: 'Cisco',
    topic: 'Computer Networks',
    targetRole: 'Network Software Engineer',
    difficulty: 'EASY',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Compare HTTP/3 over QUIC with HTTP/2 over TCP. How does QUIC eliminate transport-layer Head-of-Line (HoL) blocking?',
    constraints: [
      'Compare TCP vs UDP/QUIC multiplexing',
      'Explain single packet drop consequences in HTTP/2 vs HTTP/3',
    ],
    solutionExplanation:
      'HTTP/2 multiplexes streams over one single TCP stream. If one TCP segment is lost, TCP pauses the entire stream while waiting for retransmission—blocking all other unrelated multiplexed streams. HTTP/3 runs over QUIC on top of UDP. QUIC assigns packet sequences per-stream. A dropped packet in Stream 3 only pauses Stream 3; Streams 1, 2, and 4 continue processing immediately without delay.',
    solutionsByLang: {
      java: `// Concept summary:
// HTTP/2: Streams A, B, C -> Shared TCP Connection (Single lost packet stalls all streams)
// HTTP/3: Streams A, B, C -> QUIC/UDP (Single lost packet only halts affected stream)`,
      cpp: `// C++ Network abstraction:
// TCP: Byte-stream oriented with strict linear ACK
// QUIC: Packet-oriented over UDP with independent encryption & stream IDs`,
      python: `# Python aioquic simulation
# pip install aioquic
# Multiple independent asynchronous streams without TCP HOL blocking`,
      javascript: `// Node.js HTTP/3 experimental client
// import { createQuicSocket } from 'net'; // Stream independence`,
    },
    starterCode: `public class Solution {
    public static String getTransportLayer(String httpVersion) {
        return "HTTP/3".equalsIgnoreCase(httpVersion) ? "QUIC (UDP)" : "TCP";
    }
}`,
    testCases: [
      { input: 'httpVersion = "HTTP/3"', expected: 'QUIC (UDP)' },
      { input: 'httpVersion = "HTTP/2"', expected: 'TCP' },
      { input: 'httpVersion = "HTTP/1.1"', expected: 'TCP' },
    ],
  },
  {
    id: 5,
    qNum: 5,
    company: 'Amazon',
    companyShort: 'Amazon',
    topic: 'System Design & Distributed Systems',
    targetRole: 'Amazon SDE I',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 1200, // 20 mins
    isDsa: false,
    question:
      'Design an idempotent payment processing microservice architecture capable of handling network partitions, duplicate webhooks, and retry storms using the Saga pattern.',
    constraints: [
      'Idempotency Key validation via Redis atomic SETNX',
      'Orchestrated or Choreographed Saga pattern with compensating transactions',
      'Graceful handling of distributed transactions',
    ],
    solutionExplanation:
      'Every client request includes an Idempotency-Key. The payment gateway performs Redis SETNX(idempotency:key, "IN_PROGRESS", 5m). If the key already exists and status is COMPLETED, it returns the cached response immediately. For multi-service workflows (Reserve Inventory -> Deduct Wallet -> Create Order), an Orchestrated Saga coordinates state transitions. If payment fails, compensating events (release inventory) restore consistency.',
    solutionsByLang: {
      java: `// Spring Boot Idempotency Handler with Redis
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired private StringRedisTemplate redis;
    @Autowired private PaymentProcessor processor;

    @PostMapping
    public ResponseEntity<?> charge(@RequestHeader("Idempotency-Key") String key, @RequestBody PaymentRequest req) {
        String lockKey = "payment:lock:" + key;
        Boolean acquired = redis.opsForValue().setIfAbsent(lockKey, "PENDING", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(acquired)) {
            return ResponseEntity.status(409).body("Payment already processing or processed");
        }
        PaymentResult res = processor.process(req);
        redis.opsForValue().set("payment:res:" + key, res.toJson(), Duration.ofDays(1));
        return ResponseEntity.ok(res);
    }
}`,
      cpp: `// C++ Microservice Idempotency Handler with hiredis
bool verifyIdempotency(redisContext* ctx, const std::string& key) {
    redisReply* reply = (redisReply*)redisCommand(ctx, "SET payment:%s PROCESSING NX EX 300", key.c_str());
    bool ok = reply && reply->str && std::string(reply->str) == "OK";
    freeReplyObject(reply);
    return ok;
}`,
      python: `# FastAPI with Redis Idempotency Lock
from fastapi import FastAPI, Header, HTTPException
import redis

r = redis.Redis()

@app.post("/payments/process")
async def process_payment(idempotency_key: str = Header(...)):
    if not r.set(f"idemp:{idempotency_key}", "LOCKED", nx=True, ex=300):
        raise HTTPException(status_code=409, detail="Duplicate or concurrent transaction")
    # Execute transaction
    return {"status": "SUCCESS"}`,
      javascript: `// Express.js Idempotency Middleware
const idempotencyMiddleware = async (req, res, next) => {
  const key = req.headers['idempotency-key'];
  if (!key) return res.status(400).send('Idempotency-Key header required');
  const acquired = await redis.set(\`idemp:\${key}\`, 'LOCKED', 'NX', 'EX', 300);
  if (!acquired) return res.status(409).send('Transaction in progress or completed');
  next();
};`,
    },
    starterCode: `public class Solution {
    public static boolean acquireIdempotencyLock(boolean keyExistsInCache) {
        return !keyExistsInCache; // Returns true if lock was acquired (first request)
    }
}`,
    testCases: [
      { input: 'keyExistsInCache = false', expected: 'true' },
      { input: 'keyExistsInCache = true', expected: 'false' },
    ],
  },
  {
    id: 6,
    qNum: 6,
    company: 'Microsoft',
    companyShort: 'Microsoft',
    topic: 'Object Oriented Design',
    targetRole: 'Microsoft Software Engineer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Explain the SOLID principles with focus on the Dependency Inversion Principle (DIP). How does DIP contrast with Dependency Injection (DI)?',
    constraints: [
      'Define High-level and Low-level abstractions',
      'Distinguish architectural principle (DIP) from design pattern (DI)',
    ],
    solutionExplanation:
      'DIP states that high-level business modules should not depend on low-level implementation modules; both must depend on abstractions. Dependency Injection (DI) is a structural implementation technique (Constructor, Setter, or Field injection) used to fulfill DIP by passing concrete dependencies into components at runtime rather than having classes instantiate them directly.',
    solutionsByLang: {
      java: `// DIP: Depend on abstraction
public interface MessageSender { void send(String to, String msg); }

// Low level implementation
public class EmailSender implements MessageSender {
    public void send(String to, String msg) { /* SMTP logic */ }
}

// High level class depends on interface, injected via Constructor DI
public class NotificationManager {
    private final MessageSender sender;
    public NotificationManager(MessageSender sender) { this.sender = sender; }
}`,
      cpp: `// C++ DIP via pure virtual interfaces
#include <memory>

class ILogger {
public:
    virtual ~ILogger() = default;
    virtual void log(const std::string& msg) = 0;
};

class OrderService {
    std::shared_ptr<ILogger> logger;
public:
    OrderService(std::shared_ptr<ILogger> l) : logger(std::move(l)) {}
};`,
      python: `# Python Protocol / Abstract Base Class (ABC)
from abc import ABC, abstractmethod

class PaymentGateway(ABC):
    @abstractmethod
    def pay(self, amount: float): pass

class CheckoutService:
    def __init__(self, gateway: PaymentGateway):
        self.gateway = gateway`,
      javascript: `// TypeScript Interface Dependency Inversion
class AuthService {
  constructor(private tokenProvider: TokenProvider) {}
}`,
    },
    starterCode: `public class Solution {
    public static boolean isFollowingDip(boolean dependsOnInterface, boolean instantiatesConcreteDirectly) {
        return dependsOnInterface && !instantiatesConcreteDirectly;
    }
}`,
    testCases: [
      { input: 'dependsOnInterface = true, instantiatesConcreteDirectly = false', expected: 'true' },
      { input: 'dependsOnInterface = false, instantiatesConcreteDirectly = true', expected: 'false' },
    ],
  },
  {
    id: 7,
    qNum: 7,
    company: 'Oracle',
    companyShort: 'Oracle',
    topic: 'Database Internals & Storage',
    targetRole: 'Oracle Database Software Engineer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Why are B+ Trees overwhelmingly preferred over B-Trees and binary Red-Black Trees for disk-based relational database indexes?',
    constraints: [
      'Explain disk page size (4KB/8KB) and fan-out',
      'Explain leaf-node doubly linked lists for range queries',
    ],
    solutionExplanation:
      '1) Massive Fan-out & Shallow Depth: A B+ Tree node matches disk page blocks (e.g. 8KB) storing hundreds of keys per node, keeping tree height <= 3 or 4 for billions of rows (only 3-4 disk seeks). Binary trees (Red-Black) require ~30 seeks. 2) Leaf Linked List: In B+ Trees, internal nodes only store routing keys, while all data resides in leaf nodes linked sequentially. Range scans (BETWEEN 10 AND 100) traverse the doubly-linked leaf list sequentially without re-visiting root or internal nodes.',
    solutionsByLang: {
      java: `// B+ Tree concept summary
// Page Size: 8KB -> Fan-out: 500 keys per node
// Height for 100,000,000 entries = log500(10^8) ≈ 3 disk reads!`,
      cpp: `// Typical B+ Tree Leaf Node structure in DBMS engines:
struct LeafNode {
    int keys[512];
    void* dataPointers[512];
    LeafNode* prev;
    LeafNode* next; // Sequential scan link
};`,
      python: `# B+ Tree range scan demo:
# Leaf traversal: current = current.next (O(K) range efficiency)`,
      javascript: `// Memory vs Disk access: Disk seek ~5ms, RAM ~100ns`,
    },
    starterCode: `public class Solution {
    public static int calculateTreeHeight(long totalRows, int fanout) {
        return (int) Math.ceil(Math.log(totalRows) / Math.log(fanout));
    }
}`,
    testCases: [
      { input: 'totalRows = 100000000, fanout = 100', expected: '4' },
      { input: 'totalRows = 1000000, fanout = 1000', expected: '2' },
    ],
  },
  {
    id: 8,
    qNum: 8,
    company: 'Walmart Global Tech',
    companyShort: 'Walmart',
    topic: 'Distributed Messaging & Queues',
    targetRole: 'Software Engineer - Distributed Systems',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Compare Apache Kafka with RabbitMQ. How does Kafka guarantee partition-level ordering and Exactly-Once Processing (EOS)?',
    constraints: [
      'Explain Broker push vs Consumer pull model',
      'Explain Partition Key hashing and transactional producer APIs',
    ],
    solutionExplanation:
      'RabbitMQ is a smart-broker, dumb-consumer model pushing messages and deleting them upon ACK. Kafka is a dumb-broker, smart-consumer distributed append-only commit log with retention policies. Kafka guarantees FIFO ordering within a single partition by hashing message keys (MurmurHash2). Exactly-Once Semantics is achieved using idempotent producers (PID + Sequence Numbers preventing duplicates) combined with Two-Phase Commit transaction coordinators across consumer offsets and producer writes.',
    solutionsByLang: {
      java: `// Kafka Exactly-Once Producer Configuration
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("enable.idempotence", "true");
props.put("transactional.id", "order-tx-group-1");

KafkaProducer<String, String> producer = new KafkaProducer<>(props);
producer.initTransactions();
try {
    producer.beginTransaction();
    producer.send(new ProducerRecord<>("orders", "customer-123", "{'amount': 99}"));
    producer.commitTransaction();
} catch (Exception e) {
    producer.abortTransaction();
}`,
      cpp: `// librdkafka Exactly Once Configuration
rd_kafka_conf_set(conf, "enable.idempotence", "true", NULL, 0);
rd_kafka_conf_set(conf, "transactional.id", "tx-id-1", NULL, 0);`,
      python: `# Python Confluent-Kafka EOS
from confluent_kafka import Producer

p = Producer({'bootstrap.servers': 'localhost:9092', 'transactional.id': 'tx-1'})
p.init_transactions()`,
      javascript: `// KafkaJS Transactional Producer
const producer = kafka.producer({ transactionalId: 'my-transactional-producer' });
await producer.connect();`,
    },
    starterCode: `public class Solution {
    public static int getPartition(String key, int numPartitions) {
        return Math.abs(key.hashCode()) % numPartitions;
    }
}`,
    testCases: [
      { input: 'key = "order_123", numPartitions = 8', expected: 'Valid partition index (0-7)' },
      { input: 'key = "user_99", numPartitions = 4', expected: 'Valid partition index (0-3)' },
    ],
  },
  {
    id: 9,
    qNum: 9,
    company: 'Cognizant',
    companyShort: 'Cognizant',
    topic: 'Spring Boot & Microservices',
    targetRole: 'GenC Next Programmer Analyst',
    difficulty: 'EASY',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Explain the Spring Bean Lifecycle, differences between Singleton and Prototype scopes, and how @Transactional proxying handles rollback.',
    constraints: [
      'Bean lifecycle: Instantiation -> Populate Properties -> BeanNameAware -> BeanPostProcessor -> init-method',
      'Explain CGLIB vs JDK Dynamic Proxies for @Transactional',
    ],
    solutionExplanation:
      'ApplicationContext manages bean lifecycles. Singleton creates one instance per Spring container; Prototype spawns a new instance on every injection. @Transactional generates dynamic AOP proxies (CGLIB subclassing target class). The proxy begins JDBC transaction, executes user method in a try-catch: committing if no error, or issuing rollback on RuntimeException / Error by default (unless rollbackFor checked exception is configured).',
    solutionsByLang: {
      java: `@Service
public class AccountService {
    @Transactional(rollbackFor = CustomException.class)
    public void executeTransfer(Long fromId, Long toId, BigDecimal amount) {
        // Intercepted by Spring CGLIB AOP Proxy -> begins JDBC transaction
        // Rollbacks on RuntimeException or CustomException
    }
}`,
      cpp: `// C++ RAII Transaction Scope Guard comparison:
struct DbTransaction {
    DbTransaction() { beginTx(); }
    ~DbTransaction() { if (!committed) rollbackTx(); }
    void commit() { commitTx(); committed = true; }
    bool committed = false;
};`,
      python: `# Python Django/SQLAlchemy transaction decorator
from django.db import transaction

@transaction.atomic
def execute_transfer():
    pass`,
      javascript: `// Node.js Prisma/Knex transaction block
await prisma.$transaction(async (tx) => {
  // atomic rollback on throw
});`,
    },
    starterCode: `public class Solution {
    public static boolean willRollback(boolean isRuntimeException, boolean rollbackForCheckedConfigured) {
        return isRuntimeException || rollbackForCheckedConfigured;
    }
}`,
    testCases: [
      { input: 'isRuntimeException = true, rollbackForCheckedConfigured = false', expected: 'true' },
      { input: 'isRuntimeException = false, rollbackForCheckedConfigured = false', expected: 'false' },
    ],
  },
  {
    id: 10,
    qNum: 10,
    company: 'Wipro',
    companyShort: 'Wipro',
    topic: 'Quantitative Aptitude',
    targetRole: 'Wipro Turbo Cadre',
    difficulty: 'EASY',
    marks: 5,
    timeLimit: 600, // 10 mins
    isDsa: false,
    question:
      'Pipe A fills a cistern in 12 hours, Pipe B fills it in 15 hours, and Pipe C empties it in 20 hours. If all three pipes are opened simultaneously, in how many hours will the cistern be completely filled?',
    constraints: [
      'Use LCM method for total work units',
      'Calculate net positive flow per hour',
    ],
    solutionExplanation:
      'LCM(12, 15, 20) = 60 units (Total Tank Capacity). Rate of A = +60/12 = +5 units/hr. Rate of B = +60/15 = +4 units/hr. Rate of C (emptying) = -60/20 = -3 units/hr. Net Rate = 5 + 4 - 3 = +6 units/hr. Total Time = Total Work / Net Rate = 60 / 6 = 10 Hours.',
    solutionsByLang: {
      java: `public class CisternCalc {
    public static double timeToFill(double a, double b, double c) {
        double netRate = (1.0/a) + (1.0/b) - (1.0/c);
        return 1.0 / netRate; // 10.0 hours
    }
}`,
      cpp: `double timeToFill(double a, double b, double c) {
    return 1.0 / ((1.0/a) + (1.0/b) - (1.0/c));
}`,
      python: `def time_to_fill(a=12, b=15, c=20):
    return 1 / ((1/a) + (1/b) - (1/c)) # 10.0 hours`,
      javascript: `const timeToFill = (a, b, c) => 1 / ((1/a) + (1/b) - (1/c)); // 10`,
    },
    starterCode: `public class Solution {
    public static int solveTankTime(int fillA, int fillB, int emptyC) {
        double netRate = (1.0 / fillA) + (1.0 / fillB) - (1.0 / emptyC);
        return (int) Math.round(1.0 / netRate);
    }
}`,
    testCases: [
      { input: 'fillA = 12, fillB = 15, emptyC = 20', expected: '10' },
      { input: 'fillA = 10, fillB = 15, emptyC = 30', expected: '7' },
    ],
  },
  {
    id: 11,
    qNum: 11,
    company: 'Accenture',
    companyShort: 'Accenture',
    topic: 'Web Architecture & APIs',
    targetRole: 'Advanced Application Engineering Analyst',
    difficulty: 'EASY',
    marks: 5,
    timeLimit: 600,
    isDsa: false,
    question:
      'Contrast REST architectural constraints with GraphQL. How does GraphQL resolve over-fetching and under-fetching in mobile applications?',
    constraints: [
      'Compare fixed endpoints vs declarative query body',
      'Explain single round-trip nested queries',
    ],
    solutionExplanation:
      'REST maps resources to fixed URIs returning full payload objects, resulting in over-fetching (retrieving 20 unused user fields) or under-fetching (requiring N+1 round trips for user + posts + comments). GraphQL exposes a single endpoint where clients submit declarative queries specifying exact desired fields in a single HTTP request.',
    solutionsByLang: {
      java: `// GraphQL Query Definition
query GetStudentDetails {
  student(id: 42) {
    name
    cgpa
    applications {
      companyName
      status
    }
  }
}`,
      cpp: `// REST requires 2 calls: GET /students/42 then GET /students/42/applications
// GraphQL resolves in 1 single POST /graphql request`,
      python: `# Python Strawberry/Graphene schema
import strawberry

@strawberry.type
class Student:
    name: str
    cgpa: float`,
      javascript: `// Apollo Client Query
const GET_DATA = gql\`
  query { student { name cgpa } }
\`;`,
    },
    starterCode: `public class Solution {
    public static boolean solvesOverfetching(String apiType) {
        return "GraphQL".equalsIgnoreCase(apiType);
    }
}`,
    testCases: [
      { input: 'apiType = "GraphQL"', expected: 'true' },
      { input: 'apiType = "REST"', expected: 'false' },
    ],
  },
  {
    id: 12,
    qNum: 12,
    company: 'JPMorgan Chase',
    companyShort: 'JPMorgan',
    topic: 'Application Security & OAuth',
    targetRole: 'Software Engineer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Explain the OAuth 2.0 Authorization Code Flow with PKCE (Proof Key for Code Exchange). What prevents token hijacking on single-page apps (SPAs)?',
    constraints: [
      'Generate code_verifier (high-entropy cryptographic random)',
      'code_challenge = BASE64URL(SHA256(code_verifier))',
    ],
    solutionExplanation:
      'SPAs cannot securely protect a client_secret. PKCE prevents authorization code interception attacks: The client generates an unguessable code_verifier, hashes it with SHA-256 to create code_challenge, and sends the challenge with initial authorization. When trading authorization_code for JWT access token, the client sends raw code_verifier. The authorization server computes SHA256(code_verifier) and compares it with the stored challenge.',
    solutionsByLang: {
      java: `// Java PKCE Helper
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);`,
      cpp: `// OpenSSL PKCE SHA-256 computation
unsigned char hash[SHA256_DIGEST_LENGTH];
SHA256((unsigned char*)code_verifier.c_str(), code_verifier.length(), hash);`,
      python: `import hashlib, base64

verifier = "random_high_entropy_secret_12345678901234567890"
challenge = base64.urlsafe_b64encode(hashlib.sha256(verifier.encode()).digest()).decode().replace('=', '')`,
      javascript: `async function generatePkceChallenge(verifier) {
  const data = new TextEncoder().encode(verifier);
  const hash = await crypto.subtle.digest('SHA-256', data);
  return btoa(String.fromCharCode(...new Uint8Array(hash))).replace(/\\+/g, '-').replace(/\\//g, '_').replace(/=+$/, '');
}`,
    },
    starterCode: `public class Solution {
    public static boolean verifyPkce(String challenge, String verifierHash) {
        return challenge != null && challenge.equals(verifierHash);
    }
}`,
    testCases: [
      { input: 'challenge = "abc123xyz", verifierHash = "abc123xyz"', expected: 'true' },
      { input: 'challenge = "abc123xyz", verifierHash = "corrupted"', expected: 'false' },
    ],
  },
  {
    id: 13,
    qNum: 13,
    company: 'Google',
    companyShort: 'Google',
    topic: 'Cloud Computing & Linux Internals',
    targetRole: 'Google Cloud Engineer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'How do Linux namespaces and cgroups facilitate container isolation in Docker? How does Kubernetes achieve zero-downtime rolling updates?',
    constraints: [
      'Linux namespaces: PID, NET, MNT, IPC, UTS, USER',
      'cgroups: memory limits, CPU throttling, I/O limits',
      'Kubernetes readiness probes & MaxUnavailable=0',
    ],
    solutionExplanation:
      'Namespaces isolate what a process can SEE (PID gives process ID 1 illusion, NET isolates virtual network interfaces, MNT isolates private root filesystems). Cgroups control what a process can USE (throttling CPU cycles, setting hard memory caps to trigger OOM). Kubernetes rolling update creates new ReplicaSet Pods and only switches Service endpoints after Readiness Probes return HTTP 200.',
    solutionsByLang: {
      java: `// Docker uses Linux clone() system call with flags:
// CLONE_NEWPID | CLONE_NEWNET | CLONE_NEWNS | CLONE_NEWIPC`,
      cpp: `// C Linux syscall:
#include <sched.h>
int pid = clone(child_func, child_stack, CLONE_NEWPID | CLONE_NEWNET | SIGCHLD, NULL);`,
      python: `# Python cgroups inspection: /sys/fs/cgroup/memory/memory.limit_in_bytes`,
      javascript: `// Kubernetes Deployment YAML Spec:
// strategy: { type: 'RollingUpdate', rollingUpdate: { maxSurge: 1, maxUnavailable: 0 } }`,
    },
    starterCode: `public class Solution {
    public static boolean isContainerHealthy(int readinessProbeCode, boolean withinMemoryLimit) {
        return readinessProbeCode == 200 && withinMemoryLimit;
    }
}`,
    testCases: [
      { input: 'readinessProbeCode = 200, withinMemoryLimit = true', expected: 'true' },
      { input: 'readinessProbeCode = 500, withinMemoryLimit = true', expected: 'false' },
    ],
  },
  {
    id: 14,
    qNum: 14,
    company: 'Goldman Sachs',
    companyShort: 'Goldman Sachs',
    topic: 'Multithreading & JMM',
    targetRole: 'Analyst (C++ / Java Low Latency)',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Explain the Java Memory Model (JMM) happens-before guarantee for volatile variables. Compare synchronized mutexes with hardware atomic CAS (Compare-And-Swap).',
    constraints: [
      'Explain CPU cache coherence and Store-Load memory barriers',
      'Explain AtomicInteger lock-free CAS spin loops',
    ],
    solutionExplanation:
      'The JMM defines thread interactions across CPU caches. Volatile writes issue a Store-Load memory barrier ensuring changes are flushed to RAM and invalidating other cores CPU L1/L2 caches. Synchronized locks acquire OS kernel mutexes, incurring thread context-switching overhead (~1-2 microseconds). Atomic classes use CPU hardware instructions (CMPXCHG on x86) executing lock-free optimistic spin loops.',
    solutionsByLang: {
      java: `public class LockFreeCounter {
    private final AtomicInteger count = new AtomicInteger(0);
    public void increment() {
        int current;
        do {
            current = count.get();
        } while (!count.compareAndSet(current, current + 1)); // Hardware CAS
    }
}`,
      cpp: `// C++11 std::atomic lock-free CAS
#include <atomic>
std::atomic<int> counter(0);

void inc() {
    int expected = counter.load();
    while (!counter.compare_exchange_weak(expected, expected + 1)) {}
}`,
      python: `# Python GIL prevents multi-core native threading without multiprocessing
# C extensions use atomic operations for GIL release`,
      javascript: `// SharedArrayBuffer with Atomics in JavaScript
Atomics.add(typedArray, 0, 1);`,
    },
    starterCode: `public class Solution {
    public static boolean casOperation(int currentMemoryVal, int expectedVal, int newVal) {
        return currentMemoryVal == expectedVal; // Simulates CAS success
    }
}`,
    testCases: [
      { input: 'currentMemoryVal = 5, expectedVal = 5, newVal = 6', expected: 'true' },
      { input: 'currentMemoryVal = 5, expectedVal = 4, newVal = 6', expected: 'false' },
    ],
  },
  {
    id: 15,
    qNum: 15,
    company: 'Apple',
    companyShort: 'Apple',
    topic: 'Memory Management & Systems',
    targetRole: 'Apple CoreOS Software Engineer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Compare Automatic Reference Counting (ARC in Swift/Obj-C) with Mark-and-Sweep Garbage Collection. How do weak references prevent retain cycles?',
    constraints: [
      'Zero runtime pause times in ARC vs stop-the-world in GC',
      'Explain weak reference zeroing',
    ],
    solutionExplanation:
      'ARC is compile-time deterministic reference counting (inserting retain/release calls during compilation). When an object retain count reaches 0, it is deallocated immediately with zero GC pause time. Strong reference cycles (Object A holds strong B, B holds strong A) prevent deallocation. Weak references do not increment the retain counter and are automatically set to nil (zeroed) when the referenced object is destroyed.',
    solutionsByLang: {
      java: `// Java uses Garbage Collector (Mark & Sweep) with WeakReference:
WeakReference<HeavyObject> weakRef = new WeakReference<>(new HeavyObject());`,
      cpp: `// C++ std::shared_ptr and std::weak_ptr prevents cycles:
#include <memory>
struct B;
struct A { std::shared_ptr<B> b; };
struct B { std::weak_ptr<A> a; }; // Weak pointer breaks cycle!`,
      python: `# Python uses Reference Counting + Generational Cycle Detector
import weakref
ref = weakref.ref(heavy_object)`,
      javascript: `// JavaScript WeakMap / WeakRef
const weakRef = new WeakRef(targetObject);`,
    },
    starterCode: `public class Solution {
    public static boolean willDeallocate(int strongReferenceCount) {
        return strongReferenceCount <= 0;
    }
}`,
    testCases: [
      { input: 'strongReferenceCount = 0', expected: 'true' },
      { input: 'strongReferenceCount = 2', expected: 'false' },
    ],
  },
  {
    id: 16,
    qNum: 16,
    company: 'Netflix',
    companyShort: 'Netflix',
    topic: 'Microservices & Resiliency',
    targetRole: 'Distributed Systems Engineer',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'Explain the Circuit Breaker pattern (Resilience4j / Hystrix). Describe the state machine transitions (CLOSED -> OPEN -> HALF-OPEN) and fallback handling.',
    constraints: [
      'Sliding window metrics (failure rate threshold e.g. 50%)',
      'Wait duration in open state before transitioning to half-open',
    ],
    solutionExplanation:
      'When microservices fail, cascading failures can bring down the cluster. Circuit Breakers start CLOSED (normal). If calls in a sliding window fail beyond a threshold (e.g. >50% failure rate), it trips to OPEN, instantly failing fast without calling downstream services. After a configured wait (e.g. 30s), it enters HALF-OPEN, allowing a trial batch of requests. If successful, it resets to CLOSED; otherwise it trips back to OPEN.',
    solutionsByLang: {
      java: `@Service
public class RecommendationService {
    @CircuitBreaker(name = "videoService", fallbackMethod = "defaultRecommendations")
    public List<Movie> getRecommendations(String userId) {
        return restTemplate.getForObject("http://video-service/api", List.class);
    }

    public List<Movie> defaultRecommendations(String userId, Throwable t) {
        return List.of(new Movie("Top Trending 2026")); // Fallback cache
    }
}`,
      cpp: `// C++ Circuit Breaker state enum:
enum class State { CLOSED, OPEN, HALF_OPEN };`,
      python: `# Python pybreaker
import pybreaker
db_breaker = pybreaker.CircuitBreaker(fail_max=5, reset_timeout=30)`,
      javascript: `// Node.js opossum circuit breaker
const CircuitBreaker = require('opossum');
const breaker = new CircuitBreaker(asyncApiCall, { timeout: 3000, errorThresholdPercentage: 50 });`,
    },
    starterCode: `public class Solution {
    public static String getNextState(String currentState, double failureRate, int secondsInOpen) {
        if ("CLOSED".equals(currentState) && failureRate > 50.0) return "OPEN";
        if ("OPEN".equals(currentState) && secondsInOpen >= 30) return "HALF-OPEN";
        return currentState;
    }
}`,
    testCases: [
      { input: 'currentState = "CLOSED", failureRate = 60.0, secondsInOpen = 0', expected: 'OPEN' },
      { input: 'currentState = "OPEN", failureRate = 0, secondsInOpen = 35', expected: 'HALF-OPEN' },
    ],
  },
  {
    id: 17,
    qNum: 17,
    company: 'Uber',
    companyShort: 'Uber',
    topic: 'Geospatial Algorithms & High Scale',
    targetRole: 'Software Engineer - Marketplace Dispatch',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'How does Uber utilize hierarchical hexagonal spatial indexing (Uber H3 / Geohash) to match riders with drivers in sub-second latency?',
    constraints: [
      'Compare standard latitude/longitude bounding boxes vs discrete hexagon tiling',
      'Explain k-ring neighbor search efficiency',
    ],
    solutionExplanation:
      'Regular latitude/longitude distance calculations using the Haversine formula require scanning all drivers with complex trigonometric formulas. Uber H3 partitions the world surface into discrete hexagonal grid cells. Hexagons have the unique property that all adjacent neighbors are equidistant. A driver coordinates map to an 8-byte 64-bit H3 index. Rider matching performs a constant-time k-ring lookup (adjacent cell rings) in in-memory Redis sets.',
    solutionsByLang: {
      java: `// Uber H3 Java Library Usage
H3Core h3 = H3Core.newInstance();
long riderCell = h3.geoToH3(26.9124, 75.7873, 8); // Jaipur coordinates
List<Long> nearbyCells = h3.kRing(riderCell, 2); // 2-ring search radius`,
      cpp: `// C H3 API
H3Index riderCell;
latLngToCell(&coord, 8, &riderCell);
gridDisk(riderCell, 2, outCells);`,
      python: `import h3
cell = h3.geo_to_h3(26.9124, 75.7873, resolution=8)
neighbors = h3.k_ring(cell, k=2)`,
      javascript: `import { latLngToCell, gridDisk } from 'h3-js';
const cell = latLngToCell(26.9124, 75.7873, 8);
const rings = gridDisk(cell, 2);`,
    },
    starterCode: `public class Solution {
    public static int getRingCellCount(int k) {
        // Number of hexagons in k-ring is 1 + 3*k*(k+1)
        return 1 + 3 * k * (k + 1);
    }
}`,
    testCases: [
      { input: 'k = 1', expected: '7' },
      { input: 'k = 2', expected: '19' },
    ],
  },
  {
    id: 18,
    qNum: 18,
    company: 'Salesforce',
    companyShort: 'Salesforce',
    topic: 'Cloud Multi-Tenancy & Databases',
    targetRole: 'Salesforce Member of Technical Staff (MTS)',
    difficulty: 'MEDIUM',
    marks: 10,
    timeLimit: 900,
    isDsa: false,
    question:
      'How does Salesforce implement a Multi-Tenant Database Architecture? Contrast Database-per-tenant, Schema-per-tenant, and Shared-Database-Shared-Schema with Org-ID partitioning.',
    constraints: [
      'Explain metadata-driven engine (Universal Data Dictionary)',
      'Security isolation across tenants sharing physical tables',
    ],
    solutionExplanation:
      'Salesforce uses Shared Database, Shared Schema with metadata virtualization. All customer entities are stored in a few massive partitioned tables (Universal Data Dictionary) where every row contains an indexed Tenant/Org_ID. Virtual query compilers inject `WHERE org_id = :contextOrgId` automatically into every database query, ensuring mathematical tenant isolation without managing thousands of separate database instances.',
    solutionsByLang: {
      java: `// Multi-tenant automatic filter injection
@Entity
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "orgId", type = String.class))
@Filter(name = "tenantFilter", condition = "org_id = :orgId")
public class TenantAwareEntity {
    @Column(name = "org_id", nullable = false)
    private String orgId;
}`,
      cpp: `// Tenant isolation abstraction:
std::string injectTenantFilter(const std::string& sql, const std::string& tenantId) {
    return sql + " WHERE tenant_id = '" + tenantId + "'";
}`,
      python: `# Django multi-tenant middleware sets connection search_path or tenant filter`,
      javascript: `// Prisma Multi-tenant client extension:
prisma.$extends({
  query: {
    $allModels: {
      async findMany({ args, query }) {
        args.where = { ...args.where, tenantId: currentTenantId };
        return query(args);
      }
    }
  }
});`,
    },
    starterCode: `public class Solution {
    public static boolean isQuerySecure(String query, String tenantId) {
        return query != null && query.contains("org_id = '" + tenantId + "'");
    }
}`,
    testCases: [
      { input: 'query = "SELECT * FROM data WHERE org_id = \'ORG_42\'", tenantId = "ORG_42"', expected: 'true' },
      { input: 'query = "SELECT * FROM data", tenantId = "ORG_42"', expected: 'false' },
    ],
  },
  {
    id: 19,
    qNum: 19,
    company: 'Google',
    companyShort: 'Google',
    topic: 'Data Structures & Algorithms (Trees & DP)',
    targetRole: 'Google Software Development Engineer (SDE-II)',
    difficulty: 'HARD',
    marks: 25,
    timeLimit: 2100, // 35 mins
    isDsa: true,
    question:
      'DSA CODING CHALLENGE 1: Binary Tree Maximum Path Sum with Arbitrary Node Traversal. Given the root of a binary tree, return the maximum path sum of any non-empty path. The path may start and end at ANY node and does NOT need to pass through the root.',
    constraints: [
      'Number of nodes in tree: [1, 3 * 10^4]',
      'Node values: [-1000, 1000]',
      'Target Time Complexity: O(N) where N is number of nodes',
      'Target Space Complexity: O(H) where H is tree height for call stack',
    ],
    solutionExplanation:
      'ALGORITHMIC INTUITION:\nWe execute a bottom-up post-order depth-first traversal (DFS). For each node, we recursively calculate the maximum contribution its left and right subtrees can deliver. If a branch sum is negative, we prune it with Math.max(0, branchSum). At each node, the maximum path passing through this node as the apex is (node.val + leftGain + rightGain). We update the global maximum path sum with this price. Then, the function returns (node.val + max(leftGain, rightGain)) to its parent, because a valid single-path can only continue down one branch.',
    solutionsByLang: {
      java: `/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 * Time Complexity: O(N) — every node visited once
 * Space Complexity: O(H) — recursion stack bounded by tree height
 */
public class Solution {
    private int maxSum = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        maxGain(root);
        return maxSum;
    }

    private int maxGain(TreeNode node) {
        if (node == null) return 0;

        // Recursively compute max gains, ignoring negative paths (< 0)
        int leftGain = Math.max(maxGain(node.left), 0);
        int rightGain = Math.max(maxGain(node.right), 0);

        // Price of the path having 'node' as the highest bridge
        int pathPrice = node.val + leftGain + rightGain;
        maxSum = Math.max(maxSum, pathPrice);

        // Return single branch contribution to parent
        return node.val + Math.max(leftGain, rightGain);
    }
}`,
      cpp: `// C++ Optimal O(N) Post-Order Traversal
#include <algorithm>
#include <climits>

struct TreeNode {
    int val;
    TreeNode *left;
    TreeNode *right;
    TreeNode(int x) : val(x), left(nullptr), right(nullptr) {}
};

class Solution {
    int maxSum = INT_MIN;
    
    int maxGain(TreeNode* node) {
        if (!node) return 0;
        int left = std::max(maxGain(node->left), 0);
        int right = std::max(maxGain(node->right), 0);
        maxSum = std::max(maxSum, node->val + left + right);
        return node->val + std::max(left, right);
    }
public:
    int maxPathSum(TreeNode* root) {
        maxGain(root);
        return maxSum;
    }
};`,
      python: `# Python 3 Optimal Solution
class Solution:
    def maxPathSum(self, root: Optional[TreeNode]) -> int:
        self.max_sum = float('-inf')

        def max_gain(node):
            if not node:
                return 0
            left_gain = max(max_gain(node.left), 0)
            right_gain = max(max_gain(node.right), 0)
            
            # Update global max path through current node
            self.max_sum = max(self.max_sum, node.val + left_gain + right_gain)
            return node.val + max(left_gain, right_gain)

        max_gain(root)
        return self.max_sum`,
      javascript: `// JavaScript ES6 Solution
var maxPathSum = function(root) {
  let maxSum = -Infinity;

  function maxGain(node) {
    if (!node) return 0;
    const left = Math.max(maxGain(node.left), 0);
    const right = Math.max(maxGain(node.right), 0);
    maxSum = Math.max(maxSum, node.val + left + right);
    return node.val + Math.max(left, right);
  }

  maxGain(root);
  return maxSum;
};`,
    },
    starterCode: `// Java 21 - Complete the Binary Tree Maximum Path Sum Solution
public class Solution {
    private int maxSum = Integer.MIN_VALUE;

    public int maxPathSum(TreeNode root) {
        // Implement bottom-up post-order DFS logic
        return 0;
    }
}`,
    testCases: [
      { input: 'root = [1,2,3]', expected: '6 (Path: 2 -> 1 -> 3)' },
      { input: 'root = [-10,9,20,null,null,15,7]', expected: '42 (Path: 15 -> 20 -> 7)' },
      { input: 'root = [-3]', expected: '-3 (Single negative node)' },
    ],
  },
  {
    id: 20,
    qNum: 20,
    company: 'Meta',
    companyShort: 'Meta',
    topic: 'Data Structures & Algorithms (Two Pointers & Monotonic Arrays)',
    targetRole: 'Meta (Facebook) Algorithm & Systems Specialist',
    difficulty: 'HARD',
    marks: 50,
    timeLimit: 2700, // 45 mins
    isDsa: true,
    question:
      'DSA CODING CHALLENGE 2 (GRAND FINALE): Trapping Rain Water with Two Pointers & Monotonic Space Optimization. Given n non-negative integers representing an elevation map where each bar has width 1, calculate how much water it can trap after raining. Follow-up: Implement strict O(1) auxiliary space and explain the mathematical invariant proof.',
    constraints: [
      'n == height.length',
      '1 <= n <= 2 * 10^5',
      '0 <= height[i] <= 10^5',
      'Strict Time Complexity: O(N) — single pass',
      'Strict Space Complexity: O(1) — two pointers without auxiliary arrays',
    ],
    solutionExplanation:
      'MATHEMATICAL INVARIANT PROOF:\nAt any index i, water trapped is strictly min(maxLeft, maxRight) - height[i]. If we maintain two pointers (left at 0, right at n-1) and their running maxima (leftMax, rightMax):\nWhen height[left] < height[right], it guarantees that leftMax < rightMax. Regardless of what taller bars lie in between, the bottleneck for position `left` is GUARANTEED to be leftMax! Therefore, water trapped at left = max(0, leftMax - height[left]). We advance left++.\nConversely, when height[right] <= height[left], the bottleneck for position `right` is guaranteed to be rightMax. We advance right--. This achieves mathematically optimal O(N) time with strict O(1) auxiliary memory.',
    solutionsByLang: {
      java: `/**
 * Constraints: n <= 2 * 10^5
 * Time Complexity: O(N) — single pass
 * Space Complexity: O(1) — strict two pointers
 */
public class Solution {
    public int trap(int[] height) {
        if (height == null || height.length < 3) return 0;

        int left = 0, right = height.length - 1;
        int leftMax = 0, rightMax = 0;
        int totalWater = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                if (height[left] >= leftMax) {
                    leftMax = height[left];
                } else {
                    totalWater += (leftMax - height[left]);
                }
                left++;
            } else {
                if (height[right] >= rightMax) {
                    rightMax = height[right];
                } else {
                    totalWater += (rightMax - height[right]);
                }
                right--;
            }
        }

        return totalWater;
    }
}`,
      cpp: `// C++ Strict O(1) Memory Solution
#include <vector>
#include <algorithm>

class Solution {
public:
    int trap(std::vector<int>& height) {
        int left = 0, right = height.size() - 1;
        int leftMax = 0, rightMax = 0;
        int water = 0;

        while (left < right) {
            if (height[left] < height[right]) {
                if (height[left] >= leftMax) leftMax = height[left];
                else water += (leftMax - height[left]);
                left++;
            } else {
                if (height[right] >= rightMax) rightMax = height[right];
                else water += (rightMax - height[right]);
                right--;
            }
        }
        return water;
    }
};`,
      python: `# Python 3 Two Pointers Strict O(1) Space
class Solution:
    def trap(self, height: List[int]) -> int:
        if not height:
            return 0
        left, right = 0, len(height) - 1
        left_max, right_max = height[left], height[right]
        water = 0

        while left < right:
            if left_max < right_max:
                left += 1
                left_max = max(left_max, height[left])
                water += left_max - height[left]
            else:
                right -= 1
                right_max = max(right_max, height[right])
                water += right_max - height[right]

        return water`,
      javascript: `// JavaScript ES6 Two Pointers O(1) Space
var trap = function(height) {
  let left = 0, right = height.length - 1;
  let leftMax = 0, rightMax = 0;
  let totalWater = 0;

  while (left < right) {
    if (height[left] < height[right]) {
      if (height[left] >= leftMax) leftMax = height[left];
      else totalWater += (leftMax - height[left]);
      left++;
    } else {
      if (height[right] >= rightMax) rightMax = height[right];
      else totalWater += (rightMax - height[right]);
      right--;
    }
  }
  return totalWater;
};`,
    },
    starterCode: `// Java 21 - Implement Trapping Rain Water in O(N) time and O(1) space
public class Solution {
    public int trap(int[] height) {
        // Implement two pointers logic
        return 0;
    }
}`,
    testCases: [
      { input: 'height = [0,1,0,2,1,0,1,3,2,1,2,1]', expected: '6 units of water trapped' },
      { input: 'height = [4,2,0,3,2,5]', expected: '9 units of water trapped' },
      { input: 'height = [3,0,2,0,4]', expected: '7 units of water trapped' },
    ],
  },
];
