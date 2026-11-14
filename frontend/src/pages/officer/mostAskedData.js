/**
 * Most Asked Interview Questions and Structured Company Profiles
 * Curated for Campus Placements (TCS, Infosys, Amazon, Google, Microsoft, Accenture, etc.)
 */

export const MOST_ASKED_QUESTIONS = [
  // ── TECHNICAL & DSA ─────────────────────────────────────────────────────────
  {
    id: 'ma-1',
    category: 'DSA',
    categoryLabel: 'Data Structures & Algorithms',
    title: 'Reverse a Linked List & Detect Cycle (Floyd\'s Tortoise and Hare)',
    companies: ['Amazon', 'Microsoft', 'TCS', 'Infosys'],
    frequency: '96% High Frequency',
    difficulty: 'EASY',
    round: 'Technical Round 1',
    description:
      'Given the head of a singly linked list, reverse the list in-place in O(N) time and O(1) space. Additionally, determine if the linked list contains a cycle and find the entry node of the cycle.',
    keyConcept: 'Pointer manipulation, Floyd\'s two-pointer algorithm (slow & fast pointers).',
    solutionSummary:
      'To reverse: maintain three pointers (prev = null, curr = head, next = null). In each step, record next = curr.next, reverse curr.next = prev, advance prev = curr and curr = next. Return prev.\n\nTo detect cycle: slow moves 1 step, fast moves 2 steps. If they meet, a cycle exists. To find entry: reset slow to head; move both slow and fast 1 step until they meet at the cycle start.',
    codeSnippet: {
      java: `public class LinkedListCycle {
    // 1. In-place reversal: O(N) Time, O(1) Space
    public ListNode reverseList(ListNode head) {
        ListNode prev = null, curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    // 2. Cycle detection: Floyd's Algorithm
    public boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true; // Cycle detected
        }
        return false;
    }
}`,
      python: `def reverse_list(head):
    prev, curr = None, head
    while curr:
        next_node = curr.next
        curr.next = prev
        prev = curr
        curr = next_node
    return prev

def has_cycle(head):
    slow, fast = head, head
    while fast and fast.next:
        slow = slow.next
        fast = fast.next.next
        if slow == fast:
            return True
    return False`,
    },
  },
  {
    id: 'ma-2',
    category: 'DSA',
    categoryLabel: 'Data Structures & Algorithms',
    title: 'Two Sum & 3-Sum (Hash Table & Two-Pointer Strategies)',
    companies: ['Google', 'Meta', 'Amazon', 'TCS Digital'],
    frequency: '94% High Frequency',
    difficulty: 'MEDIUM',
    round: 'Technical Round 1',
    description:
      'Given an array of integers and a target, find all unique combinations of two and three numbers that sum to target without duplicate triplets.',
    keyConcept: 'Hash Table complement lookup O(N) for Two Sum; Sorting + Two Pointers O(N^2) for 3-Sum.',
    solutionSummary:
      'Two Sum: Use a HashMap to store value -> index. For each element x, check if (target - x) exists in the map.\n\n3-Sum: Sort the array. Loop through index i from 0 to N-3. Skip duplicates if arr[i] == arr[i-1]. Use two pointers left = i+1 and right = N-1. If sum == 0, record triplet and increment left while skipping duplicates. If sum < 0, left++; if sum > 0, right--.',
    codeSnippet: {
      java: `import java.util.*;

public class TwoSumAndThreeSum {
    // Two Sum: O(N) time, O(N) space
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) {
                return new int[] { map.get(complement), i };
            }
            map.put(nums[i], i);
        }
        return new int[0];
    }

    // 3-Sum: O(N^2) time, O(1) extra space
    public List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int sum = nums[i] + nums[l] + nums[r];
                if (sum == 0) {
                    res.add(Arrays.asList(nums[i], nums[l], nums[r]));
                    while (l < r && nums[l] == nums[l + 1]) l++;
                    while (l < r && nums[r] == nums[r - 1]) r--;
                    l++; r--;
                } else if (sum < 0) l++;
                else r--;
            }
        }
        return res;
    }
}`,
      python: `def two_sum(nums, target):
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []

def three_sum(nums):
    nums.sort()
    res = []
    for i in range(len(nums) - 2):
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        l, r = i + 1, len(nums) - 1
        while l < r:
            total = nums[i] + nums[l] + nums[r]
            if total == 0:
                res.append([nums[i], nums[l], nums[r]])
                while l < r and nums[l] == nums[l + 1]: l += 1
                while l < r and nums[r] == nums[r - 1]: r -= 1
                l += 1; r -= 1
            elif total < 0:
                l += 1
            else:
                r -= 1
    return res`,
    },
  },
  {
    id: 'ma-3',
    category: 'DSA',
    categoryLabel: 'Data Structures & Algorithms',
    title: 'LRU Cache Design & Implementation (O(1) get & put)',
    companies: ['Amazon', 'Microsoft', 'Google', 'Walmart'],
    frequency: '92% High Frequency',
    difficulty: 'HARD',
    round: 'Technical Round 2',
    description:
      'Design a data structure that follows the constraints of a Least Recently Used (LRU) cache with O(1) average time complexity for both get(key) and put(key, value) operations.',
    keyConcept: 'Combination of Doubly Linked List (for O(1) order re-linking) + HashMap (for O(1) node lookup).',
    solutionSummary:
      'Maintain dummy head and tail nodes in a Doubly Linked List. The most recently accessed node is always moved right after head; the least recently used node is always right before tail.\n\nget(key): check HashMap. If present, extract node, move to head, return value.\nput(key, value): if exists, update value and move to head. If new, create node, put in map, add to head. If size > capacity, remove tail.prev from both list and map.',
    codeSnippet: {
      java: `import java.util.*;

public class LRUCache {
    class Node {
        int key, val;
        Node prev, next;
        Node(int k, int v) { key = k; val = v; }
    }

    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node head = new Node(0, 0);
    private final Node tail = new Node(0, 0);

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        Node n = map.get(key);
        remove(n);
        insertHead(n);
        return n.val;
    }

    public void put(int key, int value) {
        if (map.containsKey(key)) {
            remove(map.get(key));
        }
        Node n = new Node(key, value);
        insertHead(n);
        map.put(key, n);
        if (map.size() > capacity) {
            Node lru = tail.prev;
            remove(lru);
            map.remove(lru.key);
        }
    }

    private void remove(Node n) {
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }

    private void insertHead(Node n) {
        n.next = head.next;
        n.prev = head;
        head.next.prev = n;
        head.next = n;
    }
}`,
      python: `class Node:
    def __init__(self, key=0, val=0):
        self.key, self.val = key, val
        self.prev, self.next = None, None

class LRUCache:
    def __init__(self, capacity: int):
        self.cap = capacity
        self.map = {}
        self.head, self.tail = Node(), Node()
        self.head.next = self.tail
        self.tail.prev = self.head

    def get(self, key: int) -> int:
        if key not in self.map:
            return -1
        node = self.map[key]
        self._remove(node)
        self._insert_head(node)
        return node.val

    def put(self, key: int, value: int) -> None:
        if key in self.map:
            self._remove(self.map[key])
        node = Node(key, value)
        self._insert_head(node)
        self.map[key] = node
        if len(self.map) > self.cap:
            lru = self.tail.prev
            self._remove(lru)
            del self.map[lru.key]

    def _remove(self, node):
        node.prev.next = node.next
        node.next.prev = node.prev

    def _insert_head(self, node):
        node.next = self.head.next
        node.prev = self.head
        self.head.next.prev = node
        self.head.next = node`,
    },
  },

  // ── SYSTEM DESIGN & CORE CS ────────────────────────────────────────────────
  {
    id: 'ma-4',
    category: 'SYSTEM_DESIGN',
    categoryLabel: 'System Design & Core CS',
    title: 'ACID vs BASE Properties & Database Indexing (B+ Tree vs LSM Tree)',
    companies: ['Oracle', 'JPMorgan', 'TCS', 'Infosys'],
    frequency: '95% High Frequency',
    difficulty: 'MEDIUM',
    round: 'Technical Round 2',
    description:
      'Explain ACID transaction guarantees (Atomicity, Consistency, Isolation, Durability) in relational databases. How does B+ Tree indexing optimize range and equality queries compared to Log-Structured Merge (LSM) Trees in NoSQL databases?',
    keyConcept: 'Transaction isolation levels, WAL (Write-Ahead Logging), B+ Tree branching factor & disk block alignment.',
    solutionSummary:
      'ACID: Atomicity (All or nothing via undo logs/WAL), Consistency (Invariants/constraints preserved), Isolation (Dirty reads/non-repeatable reads prevented via 2PL or MVCC), Durability (Committed data survives crashes via redo log fsync).\n\nIndexing: B+ Trees store all data in linked leaf nodes and keys in internal nodes, yielding O(log_B N) disk seeks and high locality for range scans. LSM Trees append all writes to memory (MemTable) and sequentially flush to disk (SSTables), giving higher write throughput (Cassandra, RocksDB) at the expense of read compaction latency.',
    codeSnippet: {
      java: `// SQL Query Indexing Analysis Demo
// CREATE INDEX idx_student_dept_cgpa ON students(department, cgpa);
// A B+ tree composite index on (department, cgpa) satisfies:
// 1. WHERE department = 'CSE'                   -> Index Range Scan
// 2. WHERE department = 'CSE' AND cgpa >= 8.0    -> Index Range Scan
// 3. WHERE cgpa >= 8.0                          -> Index Skip Scan / Full Table Scan (Violates Leftmost Prefix Rule!)`,
      python: `# Python illustration of B+ Tree Node structure
class BPlusTreeNode:
    def __init__(self, is_leaf=False):
        self.is_leaf = is_leaf
        self.keys = []
        self.children = [] # Pointers to children if internal; data pointers if leaf
        self.next = None   # Leaf node sibling pointer for O(1) sequential range scans`,
    },
  },
  {
    id: 'ma-5',
    category: 'SYSTEM_DESIGN',
    categoryLabel: 'System Design & Core CS',
    title: 'REST vs gRPC & Microservices Resiliency (Circuit Breaker & Rate Limiting)',
    companies: ['Netflix', 'Uber', 'Accenture', 'Cognizant'],
    frequency: '90% High Frequency',
    difficulty: 'MEDIUM',
    round: 'Technical Round 2',
    description:
      'Compare REST over HTTP/1.1 (JSON payloads) with gRPC over HTTP/2 (Protocol Buffers). How does a Circuit Breaker (Resilience4j / Hystrix) prevent cascading failures in distributed systems?',
    keyConcept: 'Binary serialization, multiplexing, sliding-window failure rate tracking (CLOSED -> OPEN -> HALF_OPEN).',
    solutionSummary:
      'REST vs gRPC: REST uses textual JSON and HTTP/1.1 (head-of-line blocking). gRPC uses binary Protobuf (smaller payloads, strict contract IDL) over HTTP/2 (multiplexing multiple streams on single TCP connection, streaming capabilities).\n\nCircuit Breaker: Monitors outbound calls in a ring buffer. In CLOSED state, calls pass through. If failure rate exceeds threshold (e.g., 50%), state transitions to OPEN, immediately returning fallbacks without contacting downstream. After wait duration, state switches to HALF_OPEN to trial a few calls; if successful, resets to CLOSED.',
    codeSnippet: {
      java: `// Resilience4j CircuitBreaker Configuration in Spring Boot
@Bean
public CircuitBreakerConfig customCircuitBreaker() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50.0f)           // Open when 50% calls fail
        .waitDurationInOpenState(Duration.ofSeconds(10)) // Wait before test
        .slidingWindowType(SlidingWindowType.COUNT_BASED)
        .slidingWindowSize(20)                 // Inspect last 20 calls
        .minimumNumberOfCalls(5)
        .build();
}`,
      python: `# Conceptual Circuit Breaker State Machine
class CircuitBreaker:
    def __init__(self, failure_threshold=5, recovery_timeout=10):
        self.state = "CLOSED"  # CLOSED, OPEN, HALF_OPEN
        self.failures = 0
        self.threshold = failure_threshold
        self.recovery_timeout = recovery_timeout
        self.last_state_change = 0`,
    },
  },

  // ── HR & BEHAVIORAL ────────────────────────────────────────────────────────
  {
    id: 'ma-6',
    category: 'HR',
    categoryLabel: 'HR & Behavioral',
    title: 'Tell Me About Yourself & Walk Me Through Your Resume',
    companies: ['All Companies', 'Google', 'Amazon', 'TCS', 'Infosys'],
    frequency: '100% Guaranteed',
    difficulty: 'EASY',
    round: 'HR & Management Round',
    description:
      'The quintessential opening question in 100% of campus interviews. Evaluates communication clarity, professional self-awareness, relevant technical foundations, and project impact.',
    keyConcept: 'The "Past-Present-Future" Framework (60 to 90 seconds concise delivery).',
    solutionSummary:
      'Structure your response in 3 crisp blocks:\n1. Present: Current degree, college (JECRC University), primary technical focus (Full-Stack / Cloud / DSA).\n2. Past: 1-2 major standout projects or hackathons (quantify impact: "Built a placement portal handling 2,000 students", "Implemented JWT and Redis caching").\n3. Future: Why this company matches your career aspirations and what unique value you bring.',
    codeSnippet: {
      java: `// Recommended Answer Script (Customized for JECRC Placement):
"Hello sir/ma'am, I am Suraj Kumar, a Computer Science Engineering undergraduate with a strong foundation in Java, Data Structures, and distributed backend systems. 

Recently, I designed and deployed a comprehensive Placement Management System featuring automated eligibility engines, AI-powered interview evaluations, and multi-role dashboards. I have also solved 300+ problems across LeetCode and GeeksforGeeks, maintaining an 8.5 CGPA.

I am particularly excited about this role because your engineering team solves high-concurrency challenges at scale, which aligns directly with my passion for building resilient, high-performance software."`,
      python: `# Key Checklist to Memorize:
# [x] Keep under 90 seconds
# [x] Mention 2 specific technical skills
# [x] Mention 1 tangible project with metrics
# [x] Connect cleanly to the prospective company`,
    },
  },
  {
    id: 'ma-7',
    category: 'HR',
    categoryLabel: 'HR & Behavioral',
    title: 'STAR Method: Describe a Challenging Bug or Conflict You Resolved',
    companies: ['Amazon (LP)', 'Microsoft', 'Google', 'TCS Prime'],
    frequency: '95% High Frequency',
    difficulty: 'MEDIUM',
    round: 'Behavioral / Bar Raiser',
    description:
      'Tests conflict resolution, problem-solving under pressure, ownership, and engineering integrity using the STAR framework.',
    keyConcept: 'Situation, Task, Action, Result (Quantified outcome).',
    solutionSummary:
      'Situation: In a team hackathon, our payment microservice intermittently failed during concurrency spikes 3 hours before deadline.\nTask: As the backend lead, I needed to isolate the bottleneck without discarding our core architecture.\nAction: I inspected thread dumps, identified a database connection pool starvation issue in HikariCP, and refactored our transactional boundary while implementing connection timeouts.\nResult: Concurrency capacity increased by 300%, passing all load tests and winning 2nd place among 45 teams.',
    codeSnippet: {
      java: `// Framework Summary:
// S - Situation: Set the scene (When, Where, Constraints)
// T - Task: What was your specific responsibility?
// A - Action: What did YOU do? (Tools, debugging steps, collaboration)
// R - Result: Quantifiable outcome, lesson learned`,
      python: `# Key pitfalls to avoid:
# - Don't blame teammates
# - Don't say "we did this" throughout; highlight your specific contribution
# - End on a positive, objective result`,
    },
  },

  // ── APTITUDE & QUANTITATIVE ────────────────────────────────────────────────
  {
    id: 'ma-8',
    category: 'APTITUDE',
    categoryLabel: 'Quantitative & Aptitude',
    title: 'Time & Work: Efficiency, Pipes & Cisterns, and Alternating Days',
    companies: ['TCS NQT', 'Infosys DSE', 'Cognizant GenC', 'Wipro NLTH'],
    frequency: '95% High Frequency',
    difficulty: 'EASY',
    round: 'Online Assessment (OA)',
    description:
      'A can complete a piece of work in 12 days, B in 15 days, and C in 20 days. They work together for 2 days, then A leaves. How many days will B and C take to finish the remaining work?',
    keyConcept: 'LCM method for Total Work units; Rate of work = Total Work / Days.',
    solutionSummary:
      '1. Find LCM of 12, 15, 20 = 60 units (Total Work).\n2. Daily efficiencies:\n   - A\'s rate = 60 / 12 = 5 units/day\n   - B\'s rate = 60 / 15 = 4 units/day\n   - C\'s rate = 60 / 20 = 3 units/day\n3. Combined rate (A + B + C) = 5 + 4 + 3 = 12 units/day.\n4. In first 2 days, work done = 2 * 12 = 24 units.\n5. Remaining work = 60 - 24 = 36 units.\n6. Rate of (B + C) = 4 + 3 = 7 units/day.\n7. Days required = 36 / 7 = 5 (1/7) days.',
    codeSnippet: {
      java: `public class TimeAndWork {
    public static double calculateDays() {
        int lcm = 60; // Total work
        int rateA = 60 / 12; // 5
        int rateB = 60 / 15; // 4
        int rateC = 60 / 20; // 3

        int workIn2Days = (rateA + rateB + rateC) * 2; // 24
        int remainingWork = lcm - workIn2Days;          // 36
        return (double) remainingWork / (rateB + rateC); // 36 / 7 = 5.14 days
    }
}`,
      python: `def remaining_days():
    total_work = 60 # LCM(12, 15, 20)
    rate_a = total_work // 12
    rate_b = total_work // 15
    rate_c = total_work // 20
    work_done = (rate_a + rate_b + rate_c) * 2
    return (total_work - work_done) / (rate_b + rate_c) # 36 / 7 = 5.14`,
    },
  },
];

export const COMPANY_DIRECTORY_PROFILES = [
  {
    id: 'tcs',
    name: 'Tata Consultancy Services',
    shortName: 'TCS',
    logoLetter: 'T',
    color: '#3b82f6',
    bgColor: 'rgba(59, 130, 246, 0.15)',
    packageTier: '3.36 - 9.0 LPA',
    roles: ['Ninja (3.36 LPA)', 'Digital (7.5 LPA)', 'Prime (9.0 LPA)'],
    minCgpa: 6.5,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'TCS NQT (National Qualifier Test)',
        type: 'Online Assessment',
        duration: '165 Mins',
        focus: 'Numerical Ability, Reasoning Ability, Verbal Ability, Advanced Coding (2 Problems).',
      },
      {
        roundNum: 2,
        title: 'Technical Interview',
        type: 'F2F / Video Interview',
        duration: '30-45 Mins',
        focus: 'Java/Python fundamentals, OOP, SQL queries & indexing, HashMap internals, project walkthrough.',
      },
      {
        roundNum: 3,
        title: 'Managerial & HR Discussion',
        type: 'Behavioral & Fitment',
        duration: '20-30 Mins',
        focus: 'Relocation willingness, situational ethics, shift flexibility, career goals.',
      },
    ],
  },
  {
    id: 'infosys',
    name: 'Infosys Limited',
    shortName: 'Infosys',
    logoLetter: 'I',
    color: '#06b6d4',
    bgColor: 'rgba(6, 182, 212, 0.15)',
    packageTier: '3.6 - 9.5 LPA',
    roles: ['Systems Engineer (3.6 LPA)', 'DSE (6.25 LPA)', 'Specialist Programmer (9.5 LPA)'],
    minCgpa: 6.8,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'InfyTQ / Certification Exam',
        type: 'Online Coding & MCQ',
        duration: '180 Mins',
        focus: 'Data Structures & Algorithms, Hands-on Coding (Python/Java), DBMS & SQL, Pseudocode.',
      },
      {
        roundNum: 2,
        title: 'Technical Round (DSA & Architecture)',
        type: 'Technical Interview',
        duration: '45 Mins',
        focus: 'Graph traversals, Dynamic Programming, Spring Boot REST APIs, Saga Pattern in Microservices.',
      },
      {
        roundNum: 3,
        title: 'HR & Cultural Alignment',
        type: 'HR Round',
        duration: '20 Mins',
        focus: 'Communication skills, team orientation, adaptability, continuous learning mindset.',
      },
    ],
  },
  {
    id: 'amazon',
    name: 'Amazon Development Centre',
    shortName: 'Amazon',
    logoLetter: 'A',
    color: '#f59e0b',
    bgColor: 'rgba(245, 158, 11, 0.15)',
    packageTier: '28 - 45 LPA',
    roles: ['Software Development Engineer I (SDE-1)', 'Cloud Support Associate'],
    minCgpa: 7.5,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Online Assessment (Hackerrank)',
        type: 'OA & Work Style',
        duration: '90 Mins',
        focus: '2 Medium/Hard DSA Coding questions + Amazon Work Style Simulation (Leadership Principles).',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (Data Structures)',
        type: 'Live Coding',
        duration: '60 Mins',
        focus: 'Trees, Heaps, Two Pointers, Trapping Rain Water, LRU Cache implementation.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Algorithms & LLD)',
        type: 'Live Coding & Design',
        duration: '60 Mins',
        focus: 'Dynamic Programming, Graph Shortest Path, Low-Level Object Oriented Design (Parking Lot / Snake & Ladder).',
      },
      {
        roundNum: 4,
        title: 'Bar Raiser & Leadership Principles',
        type: 'Behavioral & Technical',
        duration: '60 Mins',
        focus: 'Deep STAR questions on Customer Obsession, Ownership, Bias for Action, and Deliver Results.',
      },
    ],
  },
  {
    id: 'google',
    name: 'Google LLC',
    shortName: 'Google',
    logoLetter: 'G',
    color: '#ef4444',
    bgColor: 'rgba(239, 68, 68, 0.15)',
    packageTier: '32 - 55 LPA',
    roles: ['Software Engineer (L3)', 'Associate Cloud Engineer'],
    minCgpa: 8.0,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Google Online Challenge',
        type: 'OA Coding',
        duration: '90 Mins',
        focus: '2 Algorithmic challenges (Segment Trees, Trie, Hard Dynamic Programming).',
      },
      {
        roundNum: 2,
        title: 'Technical Interview 1 (Algorithms)',
        type: 'Live Google Docs Coding',
        duration: '45 Mins',
        focus: 'Optimal time & space complexity, edge cases, clear thought articulation, clean modular code.',
      },
      {
        roundNum: 3,
        title: 'Technical Interview 2 (Data Structures & Scale)',
        type: 'Live Coding',
        duration: '45 Mins',
        focus: 'Concurrency, distributed consensus (Raft), memory layout, amortized complexity analysis.',
      },
      {
        roundNum: 4,
        title: 'Googliness & Leadership',
        type: 'Behavioral',
        duration: '45 Mins',
        focus: 'Navigating ambiguity, intellectual humility, doing the right thing, collaborative problem-solving.',
      },
    ],
  },
  {
    id: 'microsoft',
    name: 'Microsoft Corporation',
    shortName: 'Microsoft',
    logoLetter: 'M',
    color: '#10b981',
    bgColor: 'rgba(16, 185, 129, 0.15)',
    packageTier: '25 - 45 LPA',
    roles: ['Software Engineer', 'Support Escalation Engineer'],
    minCgpa: 7.5,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Codility Online Assessment',
        type: 'OA Coding',
        duration: '90 Mins',
        focus: '3 Algorithmic problems covering Array, String, and Tree manipulation.',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (Core DSA)',
        type: 'Live Coding',
        duration: '60 Mins',
        focus: 'Binary Search Trees, Graph BFS/DFS, In-place linked list reversal, bit manipulation.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (OS & System Internals)',
        type: 'Systems & Coding',
        duration: '60 Mins',
        focus: 'Virtual Memory, Page Fault handling, TLB, Multi-threading, Deadlock prevention.',
      },
      {
        roundNum: 4,
        title: 'AA Round (As-Appropriate / Partner Director)',
        type: 'System Design & Fitment',
        duration: '45 Mins',
        focus: 'High-level system design, growth mindset, technical passion projects, cultural fit.',
      },
    ],
  },
  {
    id: 'accenture',
    name: 'Accenture Solutions',
    shortName: 'Accenture',
    logoLetter: 'A',
    color: '#a855f7',
    bgColor: 'rgba(168, 85, 247, 0.15)',
    packageTier: '4.5 - 6.5 LPA',
    roles: ['Associate Software Engineer (ASE)', 'Advanced ASE'],
    minCgpa: 6.5,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'Cognitive & Technical Assessment',
        type: 'OA MCQ + Coding',
        duration: '135 Mins',
        focus: 'English Ability, Critical Thinking, Abstract Reasoning, Common Application Security, 2 Coding tasks.',
      },
      {
        roundNum: 2,
        title: 'Communication Assessment',
        type: 'Automated Speaking & Listening',
        duration: '20 Mins',
        focus: 'Pronunciation, fluency, vocabulary, grammar comprehension.',
      },
      {
        roundNum: 3,
        title: 'Technical & HR Interview',
        type: 'Virtual Interview',
        duration: '25-35 Mins',
        focus: 'Final year projects, DevOps lifecycle, cloud fundamentals (AWS/Azure), behavioral fit.',
      },
    ],
  },
  {
    id: 'meta',
    name: 'Meta Platforms (Facebook)',
    shortName: 'Meta',
    logoLetter: 'M',
    color: '#0284c7',
    bgColor: 'rgba(2, 132, 199, 0.15)',
    packageTier: '35 - 55 LPA',
    roles: ['Software Engineer', 'Production Engineer'],
    minCgpa: 8.0,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Meta Coding Assessment',
        type: 'Online Coding',
        duration: '60 Mins',
        focus: '2 Medium/Hard algorithmic challenges (Dynamic Programming, Graph traversals).',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (Algorithms)',
        type: 'Live Coding',
        duration: '45 Mins',
        focus: 'Optimal Big-O complexity, edge cases, rapid bug-free implementation.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Systems & DSA)',
        type: 'Live Coding & Design',
        duration: '45 Mins',
        focus: 'Data pipelines, distributed cache, Longest Increasing Subsequence.',
      },
      {
        roundNum: 4,
        title: 'Behavioral & Leadership',
        type: 'Culture & Fit',
        duration: '45 Mins',
        focus: 'Move fast, be bold, resolve ambiguous team challenges.',
      },
    ],
  },
  {
    id: 'goldman-sachs',
    name: 'Goldman Sachs',
    shortName: 'Goldman Sachs',
    logoLetter: 'G',
    color: '#d97706',
    bgColor: 'rgba(217, 119, 6, 0.15)',
    packageTier: '24 - 32 LPA',
    roles: ['Engineering Analyst', 'Operations Analyst'],
    minCgpa: 7.5,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Aptitude & Math Assessment',
        type: 'Online MCQ + Coding',
        duration: '90 Mins',
        focus: 'Advanced Quantitative Aptitude, Probability, Linear Algebra, 2 Coding tasks.',
      },
      {
        roundNum: 2,
        title: 'CoderPad Live Coding',
        type: 'Technical Interview',
        duration: '60 Mins',
        focus: 'Low-latency order matching, hash tables, high-throughput memory optimizations.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Core CS)',
        type: 'Core CS Interview',
        duration: '60 Mins',
        focus: 'DBMS indexing, multithreading, race condition handling, OOP design.',
      },
      {
        roundNum: 4,
        title: 'Managing Director & HR Round',
        type: 'Fitment',
        duration: '45 Mins',
        focus: 'Financial ethics, situational pressure, career objectives.',
      },
    ],
  },
  {
    id: 'jpmorgan',
    name: 'JPMorgan Chase & Co.',
    shortName: 'JPMorgan',
    logoLetter: 'J',
    color: '#0284c7',
    bgColor: 'rgba(2, 132, 199, 0.15)',
    packageTier: '14 - 18 LPA',
    roles: ['Software Engineer (SEP)', 'Data Analyst'],
    minCgpa: 7.0,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'Code for Good Hackathon / OA',
        type: 'Hackathon & Coding',
        duration: '24 Hours / 90 Mins',
        focus: 'Full-stack application development, collaborative teamwork, algorithmic problem solving.',
      },
      {
        roundNum: 2,
        title: 'Technical Interview',
        type: 'Live Interview',
        duration: '45 Mins',
        focus: 'High-frequency concurrency, Spring Boot microservices, ACID transaction isolation.',
      },
      {
        roundNum: 3,
        title: 'Behavioral & Leadership',
        type: 'HR Interview',
        duration: '30 Mins',
        focus: 'Financial compliance awareness, collaboration, customer-centric mindset.',
      },
    ],
  },
  {
    id: 'oracle',
    name: 'Oracle Corporation',
    shortName: 'Oracle',
    logoLetter: 'O',
    color: '#dc2626',
    bgColor: 'rgba(220, 38, 38, 0.15)',
    packageTier: '12 - 20 LPA',
    roles: ['Associate Software Engineer', 'Cloud Engineer'],
    minCgpa: 7.0,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'Online Aptitude & Coding',
        type: 'OA Assessment',
        duration: '100 Mins',
        focus: 'Core Java, SQL Query Optimization, Data Structures (Trees/Graphs).',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (Database & OS)',
        type: 'Live Technical',
        duration: '60 Mins',
        focus: 'B+ Tree indexing vs LSM Trees, WAL, Concurrency Control, Deadlock detection.',
      },
      {
        roundNum: 3,
        title: 'Technical & HR Discussion',
        type: 'Managerial',
        duration: '30 Mins',
        focus: 'System architecture, project deep-dive, enterprise cloud reliability.',
      },
    ],
  },
  {
    id: 'walmart',
    name: 'Walmart Global Tech',
    shortName: 'Walmart',
    logoLetter: 'W',
    color: '#2563eb',
    bgColor: 'rgba(37, 99, 235, 0.15)',
    packageTier: '18 - 26 LPA',
    roles: ['Software Engineer II', 'Data Engineer'],
    minCgpa: 7.5,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Online Coding Challenge',
        type: 'OA Coding',
        duration: '90 Mins',
        focus: '2 DSA problems (Dynamic Programming, Two Pointers) + Computer Science MCQs.',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (DSA)',
        type: 'Live Coding',
        duration: '60 Mins',
        focus: 'Graph traversals, Stack, Queues, Sliding Window, memory complexity.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Systems & Distributed Caching)',
        type: 'System Design',
        duration: '60 Mins',
        focus: 'Inventory resiliency, Redis distributed locking, flash sale concurrency.',
      },
      {
        roundNum: 4,
        title: 'Director & Culture Fit',
        type: 'Behavioral',
        duration: '45 Mins',
        focus: 'Customer-first mindset, ownership, technical curiosity.',
      },
    ],
  },
  {
    id: 'cognizant',
    name: 'Cognizant Technology Solutions',
    shortName: 'Cognizant',
    logoLetter: 'C',
    color: '#0891b2',
    bgColor: 'rgba(8, 145, 178, 0.15)',
    packageTier: '4.0 - 6.75 LPA',
    roles: ['GenC (4.0 LPA)', 'GenC Next (6.75 LPA)'],
    minCgpa: 6.5,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'Aptitude & Coding Assessment',
        type: 'Online Test',
        duration: '120 Mins',
        focus: 'Quantitative aptitude, logical reasoning, verbal comprehension, 2 coding tasks.',
      },
      {
        roundNum: 2,
        title: 'Technical Interview',
        type: 'Live Technical',
        duration: '30-45 Mins',
        focus: 'Java, OOP concepts, SQL queries, Spring Boot Resilience4j Circuit Breaker.',
      },
      {
        roundNum: 3,
        title: 'HR & Communication Round',
        type: 'HR Interview',
        duration: '20 Mins',
        focus: 'Relocation preferences, shift flexibility, communication confidence.',
      },
    ],
  },
  {
    id: 'wipro',
    name: 'Wipro Limited',
    shortName: 'Wipro',
    logoLetter: 'W',
    color: '#059669',
    bgColor: 'rgba(5, 150, 105, 0.15)',
    packageTier: '3.5 - 6.5 LPA',
    roles: ['Elite (3.5 LPA)', 'Turbo (6.5 LPA)'],
    minCgpa: 6.5,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'National Talent Hunt (NLTH)',
        type: 'Online Test',
        duration: '128 Mins',
        focus: 'Aptitude, Essay Writing, 2 Coding problems (Arrays & Strings).',
      },
      {
        roundNum: 2,
        title: 'Technical Interview',
        type: 'Virtual Interview',
        duration: '30 Mins',
        focus: 'Enterprise integration, Kafka partitioning & idempotency, core Java.',
      },
      {
        roundNum: 3,
        title: 'HR Interview',
        type: 'Fitment',
        duration: '20 Mins',
        focus: 'Domain interests, soft skills, willingness to relocate.',
      },
    ],
  },
  {
    id: 'cisco',
    name: 'Cisco Systems',
    shortName: 'Cisco',
    logoLetter: 'C',
    color: '#0284c7',
    bgColor: 'rgba(2, 132, 199, 0.15)',
    packageTier: '14 - 22 LPA',
    roles: ['Technical Consulting Engineer', 'Software Engineer'],
    minCgpa: 7.0,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'Networking & Coding Assessment',
        type: 'OA Assessment',
        duration: '90 Mins',
        focus: 'Computer Networks (TCP/IP, Subnetting, Routing), OS, 2 Coding challenges.',
      },
      {
        roundNum: 2,
        title: 'Technical Interview 1 (Networks & Concurrency)',
        type: 'Live Technical',
        duration: '60 Mins',
        focus: 'Packet processing, Linux epoll/select, socket programming, concurrency.',
      },
      {
        roundNum: 3,
        title: 'Managerial & HR Interview',
        type: 'HR & Fitment',
        duration: '30 Mins',
        focus: 'Real-world problem troubleshooting, customer empathy, collaboration.',
      },
    ],
  },
  {
    id: 'hcltech',
    name: 'HCLTech',
    shortName: 'HCLTech',
    logoLetter: 'H',
    color: '#6366f1',
    bgColor: 'rgba(99, 102, 241, 0.15)',
    packageTier: '3.5 - 6.0 LPA',
    roles: ['Software Engineer', 'Graduate Trainee'],
    minCgpa: 6.5,
    maxBacklogs: 0,
    roundsCount: 3,
    rounds: [
      {
        roundNum: 1,
        title: 'Aptitude & Technical Assessment',
        type: 'OA Assessment',
        duration: '90 Mins',
        focus: 'Aptitude, Reasoning, Verbal, Basics of Programming & Debugging.',
      },
      {
        roundNum: 2,
        title: 'Technical Interview',
        type: 'Live Interview',
        duration: '30 Mins',
        focus: 'Cloud architecture, Zero Trust security, Mutual TLS, Java/Python basics.',
      },
      {
        roundNum: 3,
        title: 'HR Discussion',
        type: 'HR Interview',
        duration: '20 Mins',
        focus: 'Willingness to learn, communication, relocation acceptance.',
      },
    ],
  },
  {
    id: 'apple',
    name: 'Apple Inc.',
    shortName: 'Apple',
    logoLetter: 'A',
    color: '#64748b',
    bgColor: 'rgba(100, 116, 139, 0.15)',
    packageTier: '30 - 52 LPA',
    roles: ['Software Engineer - CoreOS', 'Tools & Automation Engineer'],
    minCgpa: 7.8,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Apple Online Challenge',
        type: 'OA Coding',
        duration: '90 Mins',
        focus: 'Systems programming, memory management, Bitwise operations, 2 DSA questions.',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (C++/Rust/Systems)',
        type: 'Live Technical',
        duration: '60 Mins',
        focus: 'Memory safety, Automatic Reference Counting (ARC) vs Manual Allocation, pointers.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Algorithms & Architecture)',
        type: 'Live Coding',
        duration: '60 Mins',
        focus: 'Concurrency, cache-line optimization, tree/graph traversal under strict latency.',
      },
      {
        roundNum: 4,
        title: 'Team Leader & Collaboration',
        type: 'Culture & Fit',
        duration: '45 Mins',
        focus: 'Extreme attention to detail, passion for user privacy, craft perfection.',
      },
    ],
  },
  {
    id: 'netflix',
    name: 'Netflix Inc.',
    shortName: 'Netflix',
    logoLetter: 'N',
    color: '#e11d48',
    bgColor: 'rgba(225, 29, 72, 0.15)',
    packageTier: '38 - 60 LPA',
    roles: ['Software Engineer - Streaming Infrastructure'],
    minCgpa: 8.0,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Online Algorithmic Challenge',
        type: 'OA Coding',
        duration: '90 Mins',
        focus: 'Distributed streaming, graph theory, high-throughput caching.',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (System Architecture)',
        type: 'Live Technical',
        duration: '60 Mins',
        focus: 'Adaptive Bitrate Streaming (HLS/DASH), CDN caching, byte-range requests.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Distributed Systems)',
        type: 'System Design',
        duration: '60 Mins',
        focus: 'Chaos engineering, failover resiliency, global content distribution.',
      },
      {
        roundNum: 4,
        title: 'Culture Memo & Leadership Fit',
        type: 'Culture Interview',
        duration: '45 Mins',
        focus: 'Freedom and responsibility, stunning colleagues, high performance culture.',
      },
    ],
  },
  {
    id: 'uber',
    name: 'Uber Technologies',
    shortName: 'Uber',
    logoLetter: 'U',
    color: '#0f172a',
    bgColor: 'rgba(15, 23, 42, 0.15)',
    packageTier: '32 - 48 LPA',
    roles: ['Software Engineer I', 'Backend Engineer'],
    minCgpa: 7.5,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Uber HackerRank Challenge',
        type: 'OA Coding',
        duration: '90 Mins',
        focus: '2 Complex algorithmic problems (Geospatial graph, BFS/Dijkstra, Strings).',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (Geospatial & DSA)',
        type: 'Live Coding',
        duration: '60 Mins',
        focus: 'Uber H3 hexagonal indexing, spatial nearest-neighbor search, graph partitioning.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Microservices Architecture)',
        type: 'System Design',
        duration: '60 Mins',
        focus: 'Dispatch engine concurrency, driver-rider matchmaking, message queuing.',
      },
      {
        roundNum: 4,
        title: 'Hiring Manager & Values',
        type: 'Culture & Fit',
        duration: '45 Mins',
        focus: 'Customer obsession, safety first, ownership in hyper-growth environments.',
      },
    ],
  },
  {
    id: 'salesforce',
    name: 'Salesforce Inc.',
    shortName: 'Salesforce',
    logoLetter: 'S',
    color: '#0284c7',
    bgColor: 'rgba(2, 132, 199, 0.15)',
    packageTier: '26 - 38 LPA',
    roles: ['Software Development Engineer', 'MTS Associate'],
    minCgpa: 7.5,
    maxBacklogs: 0,
    roundsCount: 4,
    rounds: [
      {
        roundNum: 1,
        title: 'Online Coding Challenge',
        type: 'HackerRank OA',
        duration: '90 Mins',
        focus: '3 Algorithmic problems (Dynamic Programming, Tree Traversals, Hash Table).',
      },
      {
        roundNum: 2,
        title: 'Technical Round 1 (DSA)',
        type: 'Live Coding',
        duration: '60 Mins',
        focus: 'Clean OOP principles, unit testing mindset, optimal data structure selection.',
      },
      {
        roundNum: 3,
        title: 'Technical Round 2 (Multi-Tenancy & DB)',
        type: 'System Design',
        duration: '60 Mins',
        focus: 'Multi-tenant database isolation, custom metadata architecture, governor limits.',
      },
      {
        roundNum: 4,
        title: 'Executive Manager & Culture',
        type: 'Values Interview',
        duration: '45 Mins',
        focus: 'Trust, customer success, innovation, equality, Ohana culture.',
      },
    ],
  },
];


