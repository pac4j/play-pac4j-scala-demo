#!/bin/bash

# Script to launch play-pac4j-scala-demo and verify it works
# Usage: ./run_and_check.sh

set -e  # Stop script on error

# Ensure a compatible Java is used (prefer JDK 11) and configure sbt
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  if JAVA_17=$(/usr/libexec/java_home -v 17 2>/dev/null); then
    export JAVA_HOME="$JAVA_17"
  elif JAVA_11=$(/usr/libexec/java_home -v 11 2>/dev/null); then
    export JAVA_HOME="$JAVA_11"
  fi
  if [ -n "${JAVA_HOME:-}" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "☕ Using Java at $JAVA_HOME"
  fi
fi
# Workaround for newer JDKs removing the SecurityManager
export SBT_OPTS="${SBT_OPTS:-} -Dsbt.security.manager=false"

echo "🚀 Starting play-pac4j-scala-demo..."

# Go to project directory (one level up from ci/)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Determine which sbt command to use
if command -v sbt >/dev/null 2>&1; then
    SBT_CMD="sbt"
    echo "🛠️  Using system SBT command: $SBT_CMD"
else
    echo "📥 sbt command not found, using sbt wrapper..."
    # Download sbt launcher if not exists
    if [ ! -f "sbt" ]; then
        echo "📥 Downloading sbt launcher..."
        curl -L -o sbt "https://raw.githubusercontent.com/sbt/sbt/v1.9.6/sbt"
        chmod +x sbt
    fi
    SBT_CMD="./sbt"
    echo "🛠️  Using downloaded SBT wrapper: $SBT_CMD"
fi

# Clean and compile project
echo "📦 Compiling project..."
$SBT_CMD clean compile

# Ensure target directory exists
mkdir -p target

# Start server in background
echo "🌐 Starting server..."
$SBT_CMD run > target/server.log 2>&1 &
SERVER_PID=$!
trap 'kill $SERVER_PID 2>/dev/null || true' EXIT

# Wait for server to start (maximum 60 seconds)
echo "⏳ Waiting for server startup..."
for i in {1..60}; do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:9000 | grep -q "200"; then
        echo "✅ Server started successfully!"
        break
    fi
    if [ $i -eq 60 ]; then
        echo "❌ Timeout: Server did not start within 60 seconds"
        echo "📋 Server logs:"
        cat target/server.log
        kill $SERVER_PID 2>/dev/null || true
        exit 1
    fi
    sleep 1
done

# Verify application responds correctly
echo "🔍 Verifying HTTP response..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9000)

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Application responds with HTTP 200"
    echo "🌐 Application accessible at: http://localhost:9000"

    # Default flags
    CAS_TEST_PASSED=false
    CAS_AUTH_PASSED=false
    
    # Test clicking on casLink and following redirections to CAS login page
    echo "🔗 Testing casLink redirection to CAS login page..."
    
    # Get the casLink URL from the homepage
    CASLINK_URL="http://localhost:9000/cas/index.html"
    echo "📍 Following casLink: $CASLINK_URL"
    
    # Follow redirections and capture final URL and response
    CAS_RESPONSE=$(curl -s -L -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}" "$CASLINK_URL")
    CAS_HTTP_CODE=$(echo "$CAS_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
    CAS_FINAL_URL=$(echo "$CAS_RESPONSE" | grep "FINAL_URL:" | cut -d: -f2-)
    CAS_CONTENT=$(echo "$CAS_RESPONSE" | sed '/^FINAL_URL:/d' | sed '/^HTTP_CODE:/d')
    
    echo "🌐 Final URL: $CAS_FINAL_URL"
    echo "📄 HTTP Code: $CAS_HTTP_CODE"
    
    # Verify we reached the CAS login page
    if [ "$CAS_HTTP_CODE" = "200" ] && echo "$CAS_CONTENT" | grep -q "Enter Username & Password"; then
        echo "✅ CAS login page test passed!"
        echo "🔐 Successfully redirected to CAS login page"
        echo "📋 Page contains login form with username/password fields"
        CAS_TEST_PASSED=true

        # Simulate a CAS login using curl WITH cookies and follow redirects; then show final page content
        echo "🧪 Simulating CAS authentication via curl (with cookies, follow redirects)..."
        COOKIE_JAR="target/cookies.txt"
        CAS_LOGIN_PAGE="target/cas_login.html"
        CAS_AFTER_LOGIN="target/cas_after_login.html"
        FINAL_APP_PAGE="target/final_app.html"

        # 1) Fetch the login page (keep cookies) and capture the execution token
        echo "⬇️  Fetching CAS login page and capturing execution token..."
        curl -s -c "$COOKIE_JAR" -b "$COOKIE_JAR" -L "$CAS_FINAL_URL" -o "$CAS_LOGIN_PAGE" -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}\n" >/dev/null
        EXECUTION=$(grep -Eo 'name="execution" value="[^"]+"' "$CAS_LOGIN_PAGE" | sed -E 's/.*value=\"([^\"]+)\".*/\1/' | head -n1 || true)

        if [ -z "$EXECUTION" ]; then
            echo "❌ Could not extract CAS execution token from the login page"
            echo "   Saved page: $CAS_LOGIN_PAGE"
            CAS_AUTH_PASSED=false
        else
            echo "➡️  Submitting CAS credentials..."
            CAS_POST_RESPONSE=$(curl -s -c "$COOKIE_JAR" -b "$COOKIE_JAR" -L -o "$CAS_AFTER_LOGIN" -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}" \
                --data-urlencode "username=leleuj@gmail.com" \
                --data-urlencode "password=leleuj" \
                --data-urlencode "execution=$EXECUTION" \
                --data-urlencode "_eventId=submit" \
                "$CAS_FINAL_URL")
            CAS_POST_HTTP_CODE=$(echo "$CAS_POST_RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
            CAS_POST_FINAL_URL=$(echo "$CAS_POST_RESPONSE" | grep "FINAL_URL:" | cut -d: -f2-)

            echo "🌐 After login final URL: $CAS_POST_FINAL_URL"
            echo "📄 HTTP Code: $CAS_POST_HTTP_CODE"

            # 3) Fetch the final app page (likely http://localhost:9000/) with cookies and show content
            echo "📥 Fetching final app page content..."
            FINAL_META=$(curl -s -c "$COOKIE_JAR" -b "$COOKIE_JAR" -L -o "$FINAL_APP_PAGE" -w "FINAL_URL:%{url_effective}\nHTTP_CODE:%{http_code}" "$CAS_POST_FINAL_URL")
            FINAL_URL=$(echo "$FINAL_META" | grep "FINAL_URL:" | cut -d: -f2-)
            FINAL_APP_CODE=$(echo "$FINAL_META" | grep "HTTP_CODE:" | cut -d: -f2)

            echo "🌐 Final app URL after redirects: $FINAL_URL"
            echo "📄 Final app HTTP Code: $FINAL_APP_CODE"

            if [ "$FINAL_APP_CODE" = "200" ]; then
                echo "✅ Demo reachable after CAS login (HTTP 200)"
                CAS_AUTH_PASSED=true
                echo "----- Final page content (begin) -----"
                cat "$FINAL_APP_PAGE"
                echo ""
                echo "----- Final page content (end) -----"

                # Verify that the expected authenticated email is present in the page
                if grep -q "leleuj@gmail.com" "$FINAL_APP_PAGE"; then
                    echo "✅ Email 'leleuj@gmail.com' found in final page"
                else
                    echo "❌ Expected email 'leleuj@gmail.com' not found in final page"
                    CAS_AUTH_PASSED=false
                fi
            else
                echo "❌ Demo not reachable after CAS login (HTTP $FINAL_APP_CODE)"
                CAS_AUTH_PASSED=false
            fi
        fi
        
    else
        echo "❌ CAS login page test failed!"
        echo "🚫 Expected CAS login page but got:"
        echo "   HTTP Code: $CAS_HTTP_CODE"
        echo "   Final URL: $CAS_FINAL_URL"
        if [ ${#CAS_CONTENT} -lt 500 ]; then
            echo "   Content preview: $CAS_CONTENT"
        else
            echo "   Content preview: $(echo "$CAS_CONTENT" | head -c 500)..."
        fi
        CAS_TEST_PASSED=false
        CAS_AUTH_PASSED=false
    fi
else
    echo "❌ Initial test failed! HTTP code received: $HTTP_CODE"
    echo "📋 Server logs:"
    cat target/server.log
    CAS_TEST_PASSED=false
    CAS_AUTH_PASSED=false
fi

if [ "$HTTP_CODE" = "200" ] && [ "$CAS_TEST_PASSED" = "true" ] && [ "$CAS_AUTH_PASSED" = "true" ]; then
    echo "🎉 play-pac4j-scala-demo test completed successfully!"
    echo "✅ All tests passed:"
    echo "   - Application responds with HTTP 200"
    echo "   - CAS link redirects correctly to login page"
    echo "   - CAS login succeeds and demo is reachable"
    exit 0
else
    echo "❌ Test suite failed!"
    if [ "$HTTP_CODE" != "200" ]; then
        echo "   - Application HTTP test failed (code: $HTTP_CODE)"
    fi
    if [ "$CAS_TEST_PASSED" != "true" ]; then
        echo "   - CAS redirection test failed"
    fi
    if [ "$CAS_AUTH_PASSED" != "true" ]; then
        echo "   - CAS authentication/redirect to demo failed"
    fi
    exit 1
fi
