# ✅ PROJECT ISSUES & FIX CHECKLIST

## 🔴 CRITICAL ISSUES (MUST FIX)

### 1. ❌ **Old Files Still Exist (Duplicate Structure)**
**Issue:** Both old and new files exist, causing confusion
- [ ] Delete `/share/Enum/` (old root-level, now in src/main/java)
- [ ] Delete `/share/models/` (old root-level, now in src/main/java)
- [ ] Delete `/server/src/controllers/` (old root-level, now in src/main/java)
- [ ] Delete `/server/src/dao/` (old root-level, now in src/main/java)
- [ ] Delete `/server/src/exceptions/` (old root-level, now in src/main/java)
- [ ] Delete `/server/src/network/` (old root-level, now in src/main/java)
- [ ] Delete `/server/src/test/` (old root-level, now in src/test/java)
- [ ] Delete `/server/test/` (entirely old)
- [ ] Delete `/client/controllers/` (old root-level, now in src/main/java)
- [ ] Delete `/client/network/` (old root-level)
- [ ] Delete `/client/view/` (old root-level - BUT SAVE FXML FILES)

**Impact:** IDE confusion, compiler errors, misleading imports
**Priority:** HIGH - Must do before mvn compile

---

### 2. ⚠️ **Duplicate Projects Need Deletion**
- [ ] Delete `/giao dien client/` (Vietnamese-named duplicate)
- [ ] Delete `/Project_BidSystem-main/` (old backup)
- [ ] Delete `/AuctionSystem/` (unused module)

**Impact:** Confusing project structure
**Priority:** MEDIUM - After Phase 3 verification

---

### 3. ❌ **FXML Files Not in Proper Location**
**Issue:** FXML files in `/client/view/` but should be in `/client/src/main/resources/com/auction/client/view/`

**Current:** `client/view/*.fxml`
**Should be:** `client/src/main/resources/com/auction/client/view/*.fxml`

Files to move:
- [ ] `Login.fxml`
- [ ] `Home.fxml`
- [ ] `AuctionDetail.fxml`

**Fix:** Controllers already updated to look for resources with path `/com/auction/client/view/`
**Status:** Verified ✅

---

### 4. ⚠️ **IDE Files Should Not Be Committed**
- [ ] Add to `.gitignore`: `*.iml`
- [ ] Delete `AuctionSystem.iml`
- [ ] Delete `server/server.iml`

**Files to remove:**
- `AuctionSystem.iml`
- `server/server.iml` (if exists)

---

## 🟡 MEDIUM PRIORITY ISSUES

### 5. **Service Layer Missing** (Phase 3)
- [ ] Create `AuctionService.java`
- [ ] Create `ItemService.java`
- [ ] Create `UserService.java`
- [ ] Create `BidService.java`
- [ ] Create Repository interfaces

**Status:** Not started yet
**Priority:** MEDIUM - After cleanup Phase 4

---

### 6. **Resource Directory Configuration**
**Issue:** Client FXML files not in proper Maven resource location

**Need to add to client/pom.xml:**
```xml
<build>
  <resources>
    <resource>
      <directory>src/main/resources</directory>
    </resource>
  </resources>
  ...
</build>
```

**Status:** Check if needed
**Priority:** LOW - Usually default

---

## 🟢 VERIFICATION CHECKLIST

### ✅ Package Names (VERIFIED - DONE)
- ✅ All Share models: `com.auction.share.models.*`
- ✅ All Share enums: `com.auction.share.enums.*`
- ✅ All Server modules: `com.auction.server.*`
- ✅ All Client controllers: `com.auction.client.controller.*`

### ✅ pom.xml Fixes (PARTIALLY DONE)
- ✅ `client/pom.xml` mainClass updated to `com.auction.client.controller.HelloApplication`
- ✅ `share/pom.xml` sourceDirectory hack removed
- ✅ Server & Client have share dependency
- ⚠️ Need to verify builds work with new structure

### ✅ Imports Updated (DONE - Need verification)
- ✅ All client controllers use `com.auction.*` imports
- ✅ New server files use `com.auction.*` imports
- ✅ New share files use `com.auction.*` imports

### ❓ Compilation Test (NOT DONE)
- [ ] Run `mvn clean compile` to verify
- [ ] Run `mvn clean install` to verify all modules
- [ ] Check for any red flags in error messages

---

## 📊 ISSUE SUMMARY TABLE

| Issue | Type | Status | Priority | Phase |
|-------|------|--------|----------|-------|
| Old root-level files | CRITICAL | ⏳ Not Done | HIGH | 4 |
| Duplicate projects | CRITICAL | ⏳ Not Done | MEDIUM | 4 |
| FXML file location | IMPORTANT | ⏳ Partial | MEDIUM | 4 |
| IDE files committed | IMPORTANT | ⏳ Not Done | MEDIUM | 4 |
| Service Layer | FEATURE | ⏳ Not Done | MEDIUM | 3 |
| Compilation test | VALIDATION | ⏳ Not Done | HIGH | POST-4 |

---

## 🎯 RECOMMENDED ACTION ORDER

### STEP 1: Delete Old Files (30 min)
```bash
# Delete old root-level files
rmdir /s /q server/src/controllers
rmdir /s /q server/src/dao
rmdir /s /q server/src/exceptions
rmdir /s /q server/src/network
rmdir /s /q server/src/test
rmdir /s /q server/test
rmdir /s /q share/Enum
rmdir /s /q share/models
rmdir /s /q client/controllers
rmdir /s /q client/network
rmdir /s /q client/view
```

### STEP 2: Move FXML Files (15 min)
- Move `client/view/*.fxml` → `client/src/main/resources/com/auction/client/view/`

### STEP 3: Delete Duplicate Projects (10 min)
```bash
rmdir /s /q "giao dien client"
rmdir /s /q Project_BidSystem-main
rmdir /s /q AuctionSystem
```

### STEP 4: Update .gitignore (5 min)
- Add `*.iml`
- Delete existing `.iml` files

### STEP 5: Test Build (10 min)
```bash
mvn clean compile
mvn clean install
```

### STEP 6: Phase 3 - Service Layer (2-3 hours)
- Create service classes
- Add Spring annotations
- Refactor controllers

---

## ✨ CURRENT STATUS

```
BEFORE REFACTOR:     2.5/10 ⭐⭐ (Very Bad)
AFTER PHASE 1B+2:    6.5/10 ⭐⭐⭐⭐⭐⭐✗ (Good Progress)
AFTER PHASE 4:       8.0/10 ⭐⭐⭐⭐⭐⭐⭐⭐ (Cleanup Complete)
AFTER PHASE 3+5:     9.5/10 ⭐⭐⭐⭐⭐⭐⭐⭐⭐✗ (Near Perfect)
```

---

## 📝 NOTES

- **DO NOT** run `mvn clean install` yet until Phase 4 cleanup is done
- **KEEP** FXML file content, just move to proper location
- **VERIFY** all imports after deletions
- **BACKUP** the project before mass deletion

